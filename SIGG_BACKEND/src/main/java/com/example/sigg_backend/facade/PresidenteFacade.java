package com.example.sigg_backend.facade;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Presidente;
import com.example.sigg_backend.db.util.Conexao;

@Service
public class PresidenteFacade {

    private final MembroFacade membroFacade;

    public PresidenteFacade(MembroFacade membroFacade) {
        this.membroFacade = membroFacade;
    }

    @Transactional
    public Presidente promover(Conexao conexao, int memCod, Presidente dados) {
        Membro membro = membroFacade.buscarAtivo(conexao, memCod);
        if (!new Escoteiro().existe(conexao, memCod))
            throw new RuntimeException("Só um escoteiro pode ser promovido a presidente");
        if (dados.existe(conexao, memCod))
            throw new RuntimeException(membro.getNome() + " já é presidente");
        String problema = dados.validar();
        if (problema != null)
            throw new RuntimeException(problema);
        dados.setMembro(membro);
        if (!dados.gravar(conexao))
            throw new IllegalStateException("Não foi possível gravar o presidente");
        return dados.buscar(conexao, memCod); // devolve completo: membro, escoteiro e cargo
    }
}
