package com.example.sigg_backend.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Chefe;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;

@Service
public class ChefeFacade {

    private final MembroFacade membroFacade;

    public ChefeFacade(MembroFacade membroFacade) {
        this.membroFacade = membroFacade;
    }

    public List<Chefe> listar() {
        return new Chefe().listarChefes();
    }

    @Transactional
    public Chefe promover(int memCod, Chefe dados) {
        Membro membro = membroFacade.buscarAtivo(memCod);
        if (!new Escoteiro().existe(memCod))
            throw new RuntimeException("Só um escoteiro pode ser promovido a chefe");
        if (dados.existe(memCod))
            throw new RuntimeException(membro.getNome() + " já é chefe");
        String problema = dados.validar();
        if (problema != null)
            throw new RuntimeException(problema);
        dados.setMembro(membro);
        if (!dados.gravar())
            throw new IllegalStateException("Não foi possível gravar o chefe");
        return dados.buscar(memCod); // devolve completo: membro, escoteiro, cargo e nome da tropa
    }
}
