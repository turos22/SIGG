package com.example.sigg_backend.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Conta;
import com.example.sigg_backend.models.NovaSenhaAdmin;
import com.example.sigg_backend.models.UsuarioLogado;

@Service
public class ContaFacade {

    private final AutorizacaoFacade autorizacao;
    private final SessoesAtivas sessoes;

    public ContaFacade(AutorizacaoFacade autorizacao, SessoesAtivas sessoes) {
        this.autorizacao = autorizacao;
        this.sessoes = sessoes;
    }

    public List<Conta> listar() {
        autorizacao.exigirPerfil(List.of(UsuarioLogado.ADMIN));
        List<Conta> contas = new Conta().listar();
        List<Conta> comPerfis = new ArrayList<>();
        for (Conta conta : contas) {
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
        Conta conta = new Conta().buscarPorMembro(memCod);
        if (conta == null || !conta.existe())
            throw new NaoEncontradoException("O membro " + memCod + " não possui conta");
        conta.definirSenha(nova.getSenha());
        conta.zerarFalhas();
        if (!conta.alterar())
            throw new IllegalStateException("Não foi possível redefinir a senha");
        sessoes.encerrarTodas(String.valueOf(memCod));
        return Map.of("mensagem", "Senha redefinida. O usuário deve entrar novamente.");
    }
}
