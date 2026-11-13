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
