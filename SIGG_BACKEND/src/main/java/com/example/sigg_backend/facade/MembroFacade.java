package com.example.sigg_backend.facade;

import org.springframework.stereotype.Service;

import com.example.sigg_backend.models.Membro;


@Service
public class MembroFacade {

    public Membro buscarAtivo(int memCod) {
        Membro membro = new Membro().buscar(memCod);
        if (membro == null)
            throw new RuntimeException("Membro " + memCod + " não encontrado");
        return membro;
    }

    public void validarNovo(Membro membro) {
        if (membro == null)
            throw new RuntimeException("Os dados do membro são obrigatórios");
        String problema = membro.validar();
        if (problema != null)
            throw new RuntimeException(problema);
        if (membro.cpfJaCadastrado())
            throw new RuntimeException(
                    "Já existe um membro com esse CPF. Para dar outro papel a ele, use POST /membros/{id}/...");
        membro.setMem_cod(0);
    }
}
