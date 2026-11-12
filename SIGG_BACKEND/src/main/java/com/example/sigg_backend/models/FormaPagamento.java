package com.example.sigg_backend.models;

public class FormaPagamento {
    private int tp_cod;
    private String tp_desc;

    public FormaPagamento() {
    }

    public FormaPagamento(int tp_cod, String tp_desc) {
        this.tp_cod = tp_cod;
        this.tp_desc = tp_desc;
    }

    public int getTp_cod() {
        return tp_cod;
    }

    public void setTp_cod(int tp_cod) {
        this.tp_cod = tp_cod;
    }

    public String getTp_desc() {
        return tp_desc;
    }

    public void setTp_desc(String tp_desc) {
        this.tp_desc = tp_desc;
    }
}