package com.example.sigg_backend.facade;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Chefe;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.db.util.Conexao;

@Service
public class ChefeFacade {

    private final MembroFacade membroFacade;

    public ChefeFacade(MembroFacade membroFacade) {
        this.membroFacade = membroFacade;
    }

    @Transactional
    public Chefe promover(Conexao conexao, int memCod, Chefe dados) {
        Membro membro = membroFacade.buscarAtivo(conexao, memCod);
        if (!new Escoteiro().existe(conexao, memCod))
            throw new RuntimeException("Só um escoteiro pode ser promovido a chefe");
        if (dados.existe(conexao, memCod))
            throw new RuntimeException(membro.getNome() + " já é chefe");
        String problema = dados.validar();
        if (problema != null)
            throw new RuntimeException(problema);
        dados.setMembro(membro);
        if (!dados.gravar(conexao))
            throw new IllegalStateException("Não foi possível gravar o chefe");
        return dados.buscar(conexao, memCod); // devolve completo: membro, escoteiro, cargo e nome da tropa
    }
}
