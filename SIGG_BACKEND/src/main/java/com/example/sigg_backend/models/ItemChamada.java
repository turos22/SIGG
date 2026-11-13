package com.example.sigg_backend.models;

import java.math.BigDecimal;

// Uma linha da chamada: o escoteiro, o status na data (null se ainda não marcado) e a presença no ano
public class ItemChamada {

    private int esc_cod;
    private int mem_cod;
    private String nome;
    private String patrulha;
    private String status;
    private BigDecimal percentual_ano;

    public int getEsc_cod() {
        return esc_cod;
    }

    public void setEsc_cod(int esc_cod) {
        this.esc_cod = esc_cod;
    }

    public int getMem_cod() {
        return mem_cod;
    }

    public void setMem_cod(int mem_cod) {
        this.mem_cod = mem_cod;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPatrulha() {
        return patrulha;
    }

    public void setPatrulha(String patrulha) {
        this.patrulha = patrulha;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getPercentual_ano() {
        return percentual_ano;
    }

    public void setPercentual_ano(BigDecimal percentual_ano) {
        this.percentual_ano = percentual_ano;
    }
}
