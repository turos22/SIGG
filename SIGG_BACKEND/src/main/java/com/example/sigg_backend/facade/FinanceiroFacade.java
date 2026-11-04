package com.example.sigg_backend.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Financeiro;
import com.example.sigg_backend.models.Membro;

// Façade do caso de uso "Gerenciar Financeiros"
@Service
public class FinanceiroFacade {

    private final MembroFacade membroFacade;

    public FinanceiroFacade(MembroFacade membroFacade) {
        this.membroFacade = membroFacade;
    }

    public List<Financeiro> listar() {
        return new Financeiro().listar();
    }

    @Transactional
    public Financeiro cadastrar(Financeiro financeiro) {
        membroFacade.validarNovo(financeiro.getMembro());
        gravar(financeiro);
        return financeiro;
    }

    @Transactional
    public Financeiro acrescentar(int memCod) {
        Membro membro = membroFacade.buscarAtivo(memCod);
        Financeiro financeiro = new Financeiro();
        if (financeiro.existe(memCod))
            throw new RuntimeException(membro.getNome() + " já é financeiro");
        financeiro.setMembro(membro);
        gravar(financeiro);
        return financeiro;
    }

    private void gravar(Financeiro financeiro) {
        if (!financeiro.gravar())
            throw new IllegalStateException("Não foi possível gravar o financeiro");
    }
}
