package com.example.sigg_backend.facade;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Financeiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.db.util.Conexao;

// Façade do caso de uso "Gerenciar Financeiros"
@Service
public class FinanceiroFacade {

    private final MembroFacade membroFacade;

    public FinanceiroFacade(MembroFacade membroFacade) {
        this.membroFacade = membroFacade;
    }

    @Transactional
    public Financeiro cadastrar(Conexao conexao, Financeiro financeiro) {
        membroFacade.validarNovo(conexao, financeiro.getMembro());
        gravar(conexao, financeiro);
        return financeiro;
    }

    @Transactional
    public Financeiro acrescentar(Conexao conexao, int memCod) {
        Membro membro = membroFacade.buscarAtivo(conexao, memCod);
        Financeiro financeiro = new Financeiro();
        if (financeiro.existe(conexao, memCod))
            throw new RuntimeException(membro.getNome() + " já é financeiro");
        financeiro.setMembro(membro);
        gravar(conexao, financeiro);
        return financeiro;
    }

    private void gravar(Conexao conexao, Financeiro financeiro) {
        if (!financeiro.gravar(conexao))
            throw new IllegalStateException("Não foi possível gravar o financeiro");
    }
}
