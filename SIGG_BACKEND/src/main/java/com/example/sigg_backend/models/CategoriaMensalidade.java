package com.example.sigg_backend.models;

import java.math.BigDecimal;

import com.example.sigg_backend.db.dao.CategoriaMensalidadeDAO;

public class CategoriaMensalidade {
    private int cat_mens_cod;
    private BigDecimal valor;
    private int qtd_irmao;

    public CategoriaMensalidade() {
    }

    public CategoriaMensalidade buscar(int catMensCod) {
        return new CategoriaMensalidadeDAO().get(catMensCod);
    }

    public int getCat_mens_cod() {
        return cat_mens_cod;
    }

    public void setCat_mens_cod(int cat_mens_cod) {
        this.cat_mens_cod = cat_mens_cod;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public int getQtd_irmao() {
        return qtd_irmao;
    }

    public void setQtd_irmao(int qtd_irmao) {
        this.qtd_irmao = qtd_irmao;
    }
}
