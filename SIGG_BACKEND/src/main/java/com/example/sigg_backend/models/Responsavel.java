package com.example.sigg_backend.models;

import java.util.List;

import com.example.sigg_backend.db.dao.ResponsavelDAO;

public class Responsavel {
    private Membro membro;
    private int resp_cod;

    public Responsavel() {
    }

    public boolean gravar() {
        return new ResponsavelDAO().gravar(this);
    }

    public List<Responsavel> listar() {
        return new ResponsavelDAO().get();
    }

    public boolean existe(int memCod) {
        return new ResponsavelDAO().existe(memCod);
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
