package com.example.sigg_backend.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Presidente;

@Service
public class PresidenteFacade {

    private final MembroFacade membroFacade;

    public PresidenteFacade(MembroFacade membroFacade) {
        this.membroFacade = membroFacade;
    }

    public List<Presidente> listar() {
        return new Presidente().listarPresidentes();
    }

    @Transactional
    public Presidente promover(int memCod, Presidente dados) {
        Membro membro = membroFacade.buscarAtivo(memCod);
        if (!new Escoteiro().existe(memCod))
            throw new RuntimeException("Só um escoteiro pode ser promovido a presidente");
        if (dados.existe(memCod))
            throw new RuntimeException(membro.getNome() + " já é presidente");
        String problema = dados.validar();
        if (problema != null)
            throw new RuntimeException(problema);
        dados.setMembro(membro);
        if (!dados.gravar())
            throw new IllegalStateException("Não foi possível gravar o presidente");
        return dados.buscar(memCod); // devolve completo: membro, escoteiro e cargo
    }
}
