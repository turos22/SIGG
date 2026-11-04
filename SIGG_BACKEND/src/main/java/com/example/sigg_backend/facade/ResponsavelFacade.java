package com.example.sigg_backend.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Responsavel;

@Service
public class ResponsavelFacade {

    private final MembroFacade membroFacade;

    public ResponsavelFacade(MembroFacade membroFacade) {
        this.membroFacade = membroFacade;
    }

    public List<Responsavel> listar() {
        return new Responsavel().listar();
    }

    @Transactional
    public Responsavel cadastrar(Responsavel responsavel) {
        membroFacade.validarNovo(responsavel.getMembro());
        gravar(responsavel);
        return responsavel;
    }

    @Transactional
    public Responsavel acrescentar(int memCod) {
        Membro membro = membroFacade.buscarAtivo(memCod);
        Responsavel responsavel = new Responsavel();
        if (responsavel.existe(memCod))
            throw new RuntimeException(membro.getNome() + " já é responsável");
        responsavel.setMembro(membro);
        gravar(responsavel);
        return responsavel;
    }

    private void gravar(Responsavel responsavel) {
        if (!responsavel.gravar())
            throw new IllegalStateException("Não foi possível gravar o responsável");
    }
}
