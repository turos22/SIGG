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

@Service
public class ContaFacade {

    private static final String SEM_ADMINISTRADOR = "Deve existir ao menos um administrador ativo no sistema";

    private final AutorizacaoFacade autorizacao;
    private final SessoesAtivas sessoes;

    public ContaFacade(AutorizacaoFacade autorizacao, SessoesAtivas sessoes) {
        this.autorizacao = autorizacao;
        this.sessoes = sessoes;
    }

    public List<Conta> listar() {
        autorizacao.exigirPerfil(List.of(UsuarioLogado.ADMIN));
        List<Conta> comPerfis = new ArrayList<>();
        for (Conta conta : new Conta().listar()) {
            conta.calcularPerfis();
            comPerfis.add(conta);
        }
        return comPerfis;
    }

    @Transactional
    public Map<String, String> redefinirSenha(int memCod, NovaSenhaAdmin nova) {
        autorizacao.exigirPerfil(List.of(UsuarioLogado.ADMIN));
        if (nova == null)
            throw new RegraNegocioException("A nova senha é obrigatória");
        String problema = nova.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        Conta conta = localizar(memCod);
        conta.definirSenha(nova.getSenha());
        conta.zerarFalhas();
        salvar(conta, "Não foi possível redefinir a senha");
        sessoes.encerrarTodas(String.valueOf(memCod));
        return Map.of("mensagem", "Senha redefinida. O usuário deve entrar novamente.");
    }

    @Transactional
    public Map<String, String> alterarSituacao(int memCod, AlteracaoSituacao alteracao) {
        autorizacao.exigirPerfil(List.of(UsuarioLogado.ADMIN));
        if (alteracao == null)
            throw new RegraNegocioException("A nova situação é obrigatória");
        Conta conta = localizar(memCod);
        if (alteracao.isAtiva()) {
            conta.setSituacao(Conta.ATIVA);
            conta.zerarFalhas();
            salvar(conta, "Não foi possível ativar a conta");
            return Map.of("mensagem", "Conta ativada.");
        }
        exigirOutroAdministrador(conta);
        conta.setSituacao(Conta.INATIVA);
        salvar(conta, "Não foi possível inativar a conta");
        sessoes.encerrarTodas(String.valueOf(memCod));
        return Map.of("mensagem", "Conta inativada. As sessões abertas foram encerradas.");
    }

    @Transactional
    public Map<String, String> alterarAdministrador(int memCod, AlteracaoAdmin alteracao) {
        autorizacao.exigirPerfil(List.of(UsuarioLogado.ADMIN));
        if (alteracao == null)
            throw new RegraNegocioException("A nova situação de administrador é obrigatória");
        Conta conta = localizar(memCod);
        if (alteracao.isAdmin()) {
            if (!conta.estaAtiva())
                throw new RegraNegocioException("Ative a conta antes de torná-la administradora");
        } else {
            exigirOutroAdministrador(conta);
        }
        conta.setAdmin(alteracao.isAdmin());
        salvar(conta, "Não foi possível alterar o perfil de administrador");
        sessoes.encerrarTodas(String.valueOf(memCod));
        return Map.of("mensagem", alteracao.isAdmin()
                ? "Administrador adicionado. O usuário deve entrar novamente."
                : "Administrador removido. O usuário deve entrar novamente.");
    }

    private Conta localizar(int memCod) {
        Conta conta = new Conta().buscarPorMembro(memCod);
        if (conta == null || !conta.existe())
            throw new NaoEncontradoException("O membro " + memCod + " não possui conta");
        return conta;
    }

    private void exigirOutroAdministrador(Conta conta) {
        if (conta.isAdmin() && conta.estaAtiva() && conta.contarOutrosAdministradoresAtivos() == 0)
            throw new RegraNegocioException(SEM_ADMINISTRADOR);
    }

    private void salvar(Conta conta, String mensagemDeFalha) {
        if (!conta.alterar())
            throw new IllegalStateException(mensagemDeFalha);
    }
}
