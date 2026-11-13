package com.example.sigg_backend.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.AlteracaoAdmin;
import com.example.sigg_backend.models.AlteracaoSituacao;
import com.example.sigg_backend.models.Conta;
import com.example.sigg_backend.models.NovaSenhaAdmin;
import com.example.sigg_backend.models.UsuarioLogado;
import com.example.sigg_backend.db.util.Conexao;

@Service
public class ContaFacade {

    private static final String SEM_ADMINISTRADOR = "Deve existir ao menos um administrador ativo no sistema";

    private final AutorizacaoFacade autorizacao;
    private final SessoesAtivas sessoes;

    public ContaFacade(AutorizacaoFacade autorizacao, SessoesAtivas sessoes) {
        this.autorizacao = autorizacao;
        this.sessoes = sessoes;
    }

    public List<Conta> listar(Conexao conexao) {
        autorizacao.exigirPerfil(conexao, List.of(UsuarioLogado.ADMIN));
        List<Conta> comPerfis = new ArrayList<>();
        for (Conta conta : new Conta().listar(conexao)) {
            conta.calcularPerfis(conexao);
            comPerfis.add(conta);
        }
        return comPerfis;
    }

    @Transactional
    public Map<String, String> redefinirSenha(Conexao conexao, int memCod, NovaSenhaAdmin nova) {
        autorizacao.exigirPerfil(conexao, List.of(UsuarioLogado.ADMIN));
        if (nova == null)
            throw new RegraNegocioException("A nova senha é obrigatória");
        String problema = nova.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        Conta conta = localizar(conexao, memCod);
        conta.definirSenha(nova.getSenha());
        conta.zerarFalhas();
        salvar(conexao, conta, "Não foi possível redefinir a senha");
        sessoes.encerrarTodas(String.valueOf(memCod));
        return Map.of("mensagem", "Senha redefinida. O usuário deve entrar novamente.");
    }

    @Transactional
    public Map<String, String> alterarSituacao(Conexao conexao, int memCod, AlteracaoSituacao alteracao) {
        autorizacao.exigirPerfil(conexao, List.of(UsuarioLogado.ADMIN));
        if (alteracao == null)
            throw new RegraNegocioException("A nova situação é obrigatória");
        Conta conta = localizar(conexao, memCod);
        if (alteracao.isAtiva()) {
            conta.setSituacao(Conta.ATIVA);
            conta.zerarFalhas();
            salvar(conexao, conta, "Não foi possível ativar a conta");
            return Map.of("mensagem", "Conta ativada.");
        }
        exigirOutroAdministrador(conexao, conta);
        conta.setSituacao(Conta.INATIVA);
        salvar(conexao, conta, "Não foi possível inativar a conta");
        sessoes.encerrarTodas(String.valueOf(memCod));
        return Map.of("mensagem", "Conta inativada. As sessões abertas foram encerradas.");
    }

    @Transactional
    public Map<String, String> alterarAdministrador(Conexao conexao, int memCod, AlteracaoAdmin alteracao) {
        autorizacao.exigirPerfil(conexao, List.of(UsuarioLogado.ADMIN));
        if (alteracao == null)
            throw new RegraNegocioException("A nova situação de administrador é obrigatória");
        Conta conta = localizar(conexao, memCod);
        if (alteracao.isAdmin()) {
            if (!conta.estaAtiva())
                throw new RegraNegocioException("Ative a conta antes de torná-la administradora");
        } else {
            exigirOutroAdministrador(conexao, conta);
        }
        conta.setAdmin(alteracao.isAdmin());
        salvar(conexao, conta, "Não foi possível alterar o perfil de administrador");
        sessoes.encerrarTodas(String.valueOf(memCod));
        return Map.of("mensagem", alteracao.isAdmin()
                ? "Administrador adicionado. O usuário deve entrar novamente."
                : "Administrador removido. O usuário deve entrar novamente.");
    }

    private Conta localizar(Conexao conexao, int memCod) {
        Conta conta = new Conta().buscarPorMembro(conexao, memCod);
        if (conta == null || !conta.existe())
            throw new NaoEncontradoException("O membro " + memCod + " não possui conta");
        return conta;
    }

    private void exigirOutroAdministrador(Conexao conexao, Conta conta) {
        if (conta.isAdmin() && conta.estaAtiva() && conta.contarOutrosAdministradoresAtivos(conexao) == 0)
            throw new RegraNegocioException(SEM_ADMINISTRADOR);
    }

    private void salvar(Conexao conexao, Conta conta, String mensagemDeFalha) {
        if (!conta.alterar(conexao))
            throw new IllegalStateException(mensagemDeFalha);
    }
}
