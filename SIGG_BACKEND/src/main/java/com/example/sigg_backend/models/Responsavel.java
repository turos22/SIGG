package com.example.sigg_backend.models;

import java.util.List;

import com.example.sigg_backend.db.dao.ResponsavelDAO;
import com.example.sigg_backend.db.util.Conexao;

public class Responsavel {
    private Membro membro;
    private int resp_cod;

    public Responsavel() {
    }

    public boolean gravar(Conexao conexao) {
        return new ResponsavelDAO().gravar(conexao, this);
    }

    public List<Responsavel> listar(Conexao conexao) {
        return new ResponsavelDAO().get(conexao);
    }

    public List<Responsavel> doEscoteiro(Conexao conexao, int memCodEscoteiro) {
        return new ResponsavelDAO().doEscoteiro(conexao, memCodEscoteiro);
    }

    public boolean existeVinculo(Conexao conexao, int memCodEscoteiro) {
        return new ResponsavelDAO().existeVinculo(conexao, membro.getMem_cod(), memCodEscoteiro);
    }

    public boolean vincular(Conexao conexao, int memCodEscoteiro) {
        return new ResponsavelDAO().vincular(conexao, membro.getMem_cod(), memCodEscoteiro);
    }

    public boolean desvincular(Conexao conexao, int memCodEscoteiro) {
        return new ResponsavelDAO().desvincular(conexao, membro.getMem_cod(), memCodEscoteiro);
    }

    public int quantosVinculos(Conexao conexao) {
        return new ResponsavelDAO().contarVinculos(conexao, membro.getMem_cod());
    }

    public boolean apagar(Conexao conexao) {
        return new ResponsavelDAO().apagar(conexao, this);
    }

    public boolean existe(Conexao conexao, int memCod) {
        return new ResponsavelDAO().existe(conexao, memCod);
    }

    public Membro getMembro() {
        return membro;
    }

    public void setMembro(Membro membro) {
        this.membro = membro;
    }

    public int getResp_cod() {
        return resp_cod;
    }

    public void setResp_cod(int resp_cod) {
        this.resp_cod = resp_cod;
    }
}
