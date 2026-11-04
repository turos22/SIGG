package com.example.sigg_backend.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;


@Service
public class EscoteiroFacade {

    private final MembroFacade membroFacade;

    public EscoteiroFacade(MembroFacade membroFacade) {
        this.membroFacade = membroFacade;
    }

    public List<Escoteiro> listarTodos() {
        return new Escoteiro().listar();
    }

    public Escoteiro buscar(int memCod) {
        Escoteiro escoteiro = new Escoteiro().buscar(memCod);
        if (escoteiro == null)
            throw new RuntimeException("Escoteiro" + memCod + "não encontrado");
        return escoteiro;
    }

    @Transactional
    public Escoteiro cadastrar(Escoteiro escoteiro) {
        membroFacade.validarNovo(escoteiro.getMembro());
        validar(escoteiro);
        gravar(escoteiro);
        return escoteiro;
    }

    @Transactional
    public Escoteiro acrescentar(int memCod, Escoteiro dados) {
        Membro membro = membroFacade.buscarAtivo(memCod);
        if (dados.existe(memCod))
            throw new RuntimeException(membro.getNome() + " já é escoteiro");
        dados.setMembro(membro);
        validar(dados);
        gravar(dados);
        return dados;
    }

    private void validar(Escoteiro escoteiro) {
        String problema = escoteiro.validar();
        if (problema != null)
            throw new RuntimeException(problema);
    }

    private void gravar(Escoteiro escoteiro) {
        if (!escoteiro.gravar())
            throw new IllegalStateException("Não foi possível gravar o escoteiro");
    }
}
