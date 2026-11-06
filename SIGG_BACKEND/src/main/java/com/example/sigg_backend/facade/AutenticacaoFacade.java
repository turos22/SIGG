package com.example.sigg_backend.facade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.ConfirmacaoConta;
import com.example.sigg_backend.models.Conta;
import com.example.sigg_backend.models.CredencialLogin;
import com.example.sigg_backend.models.Cpf;
import com.example.sigg_backend.models.RedefinicaoSenha;
import com.example.sigg_backend.models.RegistroConta;
import com.example.sigg_backend.models.SolicitacaoRecuperacao;
import com.example.sigg_backend.models.TokenConta;
import com.example.sigg_backend.models.UsuarioLogado;

@Service
public class AutenticacaoFacade {

    private static final String CREDENCIAIS_INVALIDAS = "CPF ou senha inválidos, ou conta temporariamente bloqueada";
    private static final String MENSAGEM_REGISTRO =
            "Se os dados conferirem com o cadastro do grupo, enviaremos um link de confirmação para o e-mail cadastrado.";
    private static final String MENSAGEM_RECUPERACAO =
            "Se os dados conferirem, enviaremos um link para criar uma nova senha ao e-mail cadastrado.";

    private final EnvioDeEmail email;
    private final SessoesAtivas sessoes;
    private final AutorizacaoFacade autorizacao;

    public AutenticacaoFacade(EnvioDeEmail email, SessoesAtivas sessoes, AutorizacaoFacade autorizacao) {
        this.email = email;
        this.sessoes = sessoes;
        this.autorizacao = autorizacao;
    }

    public UsuarioLogado usuarioAtual() {
        return autorizacao.exigirAutenticado();
    }

    public UsuarioLogado autenticar(CredencialLogin credencial) {
        if (credencial == null || !credencial.completa())
            throw new NaoAutenticadoException(CREDENCIAIS_INVALIDAS);
        LocalDateTime agora = LocalDateTime.now();
        Conta conta = new Conta().buscarPorCpf(Cpf.somenteDigitos(credencial.getCpf()));
        boolean utilizavel = conta != null && conta.existe() && conta.estaAtiva() && conta.maiorDeIdade(agora.toLocalDate());
        if (!utilizavel) {
            Conta.gastarTempo(credencial.getSenha());
            throw new NaoAutenticadoException(CREDENCIAIS_INVALIDAS);
        }
        if (conta.estaBloqueada(agora)) {
            Conta.gastarTempo(credencial.getSenha());
            throw new NaoAutenticadoException(CREDENCIAIS_INVALIDAS);
        }
        if (!conta.senhaConfere(credencial.getSenha())) {
            conta.registrarFalha(agora);
            conta.alterar();
            throw new NaoAutenticadoException(CREDENCIAIS_INVALIDAS);
        }
        if (conta.temFalhasRegistradas()) {
            conta.zerarFalhas();
            conta.alterar();
        }
        conta.calcularPerfis();
        if (conta.getPerfis().isEmpty())
            throw new NaoAutenticadoException(CREDENCIAIS_INVALIDAS);
        return conta.paraUsuario();
    }

    @Transactional
    public Map<String, String> registrar(RegistroConta registro) {
        if (registro == null)
            throw new RegraNegocioException("Os dados do cadastro são obrigatórios");
        String problema = registro.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        Conta conta = new Conta().buscarPorCpf(Cpf.somenteDigitos(registro.getCpf()));
        if (conta == null || !dadosConferem(conta, registro.getDt_nascto(), registro.getEmail()))
            return Map.of("mensagem", MENSAGEM_REGISTRO);
        conta.calcularPerfis();
        if (conta.getPerfis().isEmpty() || (conta.existe() && !conta.estaPendente()))
            return Map.of("mensagem", MENSAGEM_REGISTRO);
        conta.definirSenha(registro.getSenha());
        boolean salva;
        if (conta.existe()) {
            salva = conta.alterar();
        } else {
            conta.setSituacao(Conta.PENDENTE);
            salva = conta.gravar();
        }
        if (!salva)
            throw new IllegalStateException("Não foi possível registrar a conta");
        String token = emitirToken(conta, TokenConta.CONFIRMACAO, LocalDateTime.now().plusHours(TokenConta.HORAS_PARA_CONFIRMAR));
        email.enviarConfirmacao(conta.membro().getEmail(), conta.membro().getNome(), token);
        return Map.of("mensagem", MENSAGEM_REGISTRO);
    }

    @Transactional
    public Map<String, String> confirmarConta(ConfirmacaoConta confirmacao) {
        LocalDateTime agora = LocalDateTime.now();
        TokenConta token = confirmacao == null ? null
                : new TokenConta().buscarValido(confirmacao.getToken(), TokenConta.CONFIRMACAO, agora);
        if (token == null)
            throw new RegraNegocioException("Link de confirmação inválido ou expirado");
        Conta conta = new Conta().buscarPorCodigo(token.getCta_cod());
        if (conta == null || !conta.estaPendente())
            throw new RegraNegocioException("Link de confirmação inválido ou expirado");
        conta.setSituacao(Conta.ATIVA);
        conta.zerarFalhas();
        if (!conta.alterar() || !token.marcarComoUsado(agora))
            throw new IllegalStateException("Não foi possível confirmar a conta");
        return Map.of("mensagem", "Conta confirmada. Você já pode entrar no sistema.");
    }

    @Transactional
    public Map<String, String> solicitarRecuperacao(SolicitacaoRecuperacao solicitacao) {
        if (solicitacao == null)
            throw new RegraNegocioException("Os dados da solicitação são obrigatórios");
        String problema = solicitacao.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        Conta conta = new Conta().buscarPorCpf(Cpf.somenteDigitos(solicitacao.getCpf()));
        boolean elegivel = conta != null && conta.existe() && conta.estaAtiva()
                && emailConfere(conta, solicitacao.getEmail());
        if (elegivel) {
            String token = emitirToken(conta, TokenConta.REDEFINICAO, LocalDateTime.now().plusMinutes(TokenConta.MINUTOS_PARA_REDEFINIR));
            email.enviarRedefinicao(conta.membro().getEmail(), conta.membro().getNome(), token);
        }
        return Map.of("mensagem", MENSAGEM_RECUPERACAO);
    }

    @Transactional
    public Map<String, String> redefinirSenha(RedefinicaoSenha redefinicao) {
        if (redefinicao == null)
            throw new RegraNegocioException("Os dados da redefinição são obrigatórios");
        String problema = redefinicao.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        LocalDateTime agora = LocalDateTime.now();
        TokenConta token = new TokenConta().buscarValido(redefinicao.getToken(), TokenConta.REDEFINICAO, agora);
        if (token == null)
            throw new RegraNegocioException("Link de redefinição inválido ou expirado");
        Conta conta = new Conta().buscarPorCodigo(token.getCta_cod());
        if (conta == null || !conta.estaAtiva())
            throw new RegraNegocioException("Link de redefinição inválido ou expirado");
        conta.definirSenha(redefinicao.getSenha());
        conta.zerarFalhas();
        if (!conta.alterar() || !token.marcarComoUsado(agora))
            throw new IllegalStateException("Não foi possível redefinir a senha");
        sessoes.encerrarTodas(String.valueOf(conta.getMem_cod()));
        return Map.of("mensagem", "Senha redefinida com sucesso. Entre com a nova senha.");
    }

    private String emitirToken(Conta conta, String tipo, LocalDateTime expira) {
        TokenConta anterior = new TokenConta();
        anterior.invalidarAnteriores(conta.getCta_cod(), tipo, LocalDateTime.now());
        String puro = TokenConta.gerarTokenPuro();
        TokenConta token = new TokenConta();
        token.setCta_cod(conta.getCta_cod());
        token.setTipo(tipo);
        token.definirHash(TokenConta.hashDe(puro));
        token.setExpira(expira);
        if (!token.gravar())
            throw new IllegalStateException("Não foi possível gerar o link de acesso");
        return puro;
    }

    private boolean dadosConferem(Conta conta, LocalDate nascimento, String emailInformado) {
        return conta.maiorDeIdade(LocalDateTime.now().toLocalDate())
                && nascimento.equals(conta.membro().getDt_nascto())
                && emailConfere(conta, emailInformado);
    }

    private boolean emailConfere(Conta conta, String emailInformado) {
        String cadastrado = conta.membro().getEmail();
        return cadastrado != null && emailInformado != null && cadastrado.trim().equalsIgnoreCase(emailInformado.trim());
    }
}
