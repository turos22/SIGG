package com.example.sigg_backend.models;

import java.util.List;

import com.example.sigg_backend.db.dao.FinanceiroDAO;

public class Financeiro {
    private Membro membro;
    private int fin_cod;

    public Financeiro() {
    }

    public boolean gravar() {
        return new FinanceiroDAO().gravar(this);
    }

    public List<Financeiro> listar() {
        return new FinanceiroDAO().get();
    }

    public boolean existe(int memCod) {
        return new FinanceiroDAO().existe(memCod);
    }

    public Membro getMembro() {
        return membro;
    }

    public void setMembro(Membro membro) {
        this.membro = membro;
    }

    public int getFin_cod() {
        return fin_cod;
    }

    public void setFin_cod(int fin_cod) {
        this.fin_cod = fin_cod;
    }
}
