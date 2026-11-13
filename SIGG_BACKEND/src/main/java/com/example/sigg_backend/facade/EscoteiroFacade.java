package com.example.sigg_backend.facade;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.db.util.Conexao;


@Service
public class EscoteiroFacade {

    private final MembroFacade membroFacade;

    public EscoteiroFacade(MembroFacade membroFacade) {
        this.membroFacade = membroFacade;
    }

    public Escoteiro buscar(Conexao conexao, int memCod) {
        Escoteiro escoteiro = new Escoteiro().buscar(conexao, memCod);
        if (escoteiro == null)
            throw new RuntimeException("Escoteiro" + memCod + "não encontrado");
        return escoteiro;
    }

    @Transactional
    public Escoteiro cadastrar(Conexao conexao, Escoteiro escoteiro) {
        membroFacade.validarNovo(conexao, escoteiro.getMembro());
        validar(escoteiro);
        gravar(conexao, escoteiro);
        return escoteiro;
    }

    @Transactional
    public Escoteiro acrescentar(Conexao conexao, int memCod, Escoteiro dados) {
        Membro membro = membroFacade.buscarAtivo(conexao, memCod);
        if (dados.existe(conexao, memCod))
            throw new RuntimeException(membro.getNome() + " já é escoteiro");
        dados.setMembro(membro);
        validar(dados);
        gravar(conexao, dados);
        return dados;
    }

    private void validar(Escoteiro escoteiro) {
        String problema = escoteiro.validar();
        if (problema != null)
            throw new RuntimeException(problema);
    }

    private void gravar(Conexao conexao, Escoteiro escoteiro) {
        if (!escoteiro.gravar(conexao))
            throw new IllegalStateException("Não foi possível gravar o escoteiro");
    }
}
