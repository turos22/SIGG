package com.example.sigg_backend.facade;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Responsavel;
import com.example.sigg_backend.db.util.Conexao;

@Service
public class ResponsavelFacade {

    private final MembroFacade membroFacade;

    public ResponsavelFacade(MembroFacade membroFacade) {
        this.membroFacade = membroFacade;
    }

    @Transactional
    public Responsavel cadastrar(Conexao conexao, Responsavel responsavel) {
        membroFacade.validarNovo(conexao, responsavel.getMembro());
        gravar(conexao, responsavel);
        return responsavel;
    }

    @Transactional
    public Responsavel acrescentar(Conexao conexao, int memCod) {
        Membro membro = membroFacade.buscarAtivo(conexao, memCod);
        Responsavel responsavel = new Responsavel();
        if (responsavel.existe(conexao, memCod))
            throw new RuntimeException(membro.getNome() + " já é responsável");
        responsavel.setMembro(membro);
        gravar(conexao, responsavel);
        return responsavel;
    }

    private void gravar(Conexao conexao, Responsavel responsavel) {
        if (!responsavel.gravar(conexao))
            throw new IllegalStateException("Não foi possível gravar o responsável");
    }
}
