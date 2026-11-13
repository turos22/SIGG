package com.example.sigg_backend.models;

import java.util.List;

import com.example.sigg_backend.db.dao.FinanceiroDAO;
import com.example.sigg_backend.db.util.Conexao;

public class Financeiro {
    private Membro membro;
    private int fin_cod;

    public Financeiro() {
    }

    public boolean gravar(Conexao conexao) {
        return new FinanceiroDAO().gravar(conexao, this);
    }

    public List<Financeiro> listar(Conexao conexao) {
        return new FinanceiroDAO().get(conexao);
    }

    public boolean existe(Conexao conexao, int memCod) {
        return new FinanceiroDAO().existe(conexao, memCod);
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
