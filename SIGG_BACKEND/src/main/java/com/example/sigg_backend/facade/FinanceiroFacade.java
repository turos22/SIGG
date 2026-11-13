package com.example.sigg_backend.facade;


import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Financeiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.UsuarioLogado;
import com.example.sigg_backend.db.util.Conexao;

// Façade do caso de uso "Gerenciar Financeiros"
@Service
public class FinanceiroFacade {

    private static final List<String> GESTORES = List.of(UsuarioLogado.ADMIN, UsuarioLogado.PRESIDENTE);

    private final MembroFacade membroFacade;
    private final AutorizacaoFacade autorizacao;

    public FinanceiroFacade(MembroFacade membroFacade, AutorizacaoFacade autorizacao) {
        this.membroFacade = membroFacade;
        this.autorizacao = autorizacao;
    }

    @Transactional
    public Financeiro cadastrar(Conexao conexao, Financeiro financeiro) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        membroFacade.exigirCpfLivre(conexao, financeiro.getMembro());
        gravar(conexao, financeiro);
        return financeiro;
    }

    @Transactional
    public Financeiro acrescentar(Conexao conexao, int memCod) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        Membro membro = membroFacade.buscarAtivo(conexao, memCod);
        Financeiro financeiro = new Financeiro();
        if (financeiro.existe(conexao, memCod))
            throw new RegraNegocioException(membro.getNome() + " já é financeiro");
        financeiro.setMembro(membro);
        gravar(conexao, financeiro);
        return financeiro;
    }

    // A tabela financeiro não tem datas de gestão: remover o cargo apaga a linha
    @Transactional
    public Map<String, String> remover(Conexao conexao, int memCod) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        Membro membro = membroFacade.buscarAtivo(conexao, memCod);
        Financeiro financeiro = new Financeiro();
        if (!financeiro.existe(conexao, memCod))
            throw new NaoEncontradoException(membro.getNome() + " não é financeiro");
        financeiro.setMembro(membro);
        if (!financeiro.apagar(conexao))
            throw new IllegalStateException("Não foi possível retirar o cargo de financeiro");
        return Map.of("mensagem", membro.getNome() + " deixou de ser financeiro");
    }

    private void gravar(Conexao conexao, Financeiro financeiro) {
        if (!financeiro.gravar(conexao))
            throw new IllegalStateException("Não foi possível gravar o financeiro");
    }
}
