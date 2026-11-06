package com.example.sigg_backend.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaldoAdotado {
    private int mem_cod;
    private int esc_cod;
    private String nome;
    private List<Mensalidade> abertas = new ArrayList<>();
    private BigDecimal subtotal = BigDecimal.ZERO.setScale(2);

    public SaldoAdotado() {
    }

    public void calcular(List<Mensalidade> mensalidades) {
        abertas = new ArrayList<>();
        subtotal = BigDecimal.ZERO.setScale(2);
        for (Mensalidade mensalidade : mensalidades) {
            if (mensalidade.estaEmAberto()) {
                abertas.add(mensalidade);
                subtotal = subtotal.add(mensalidade.saldoReal());
            }
        }
    }

    public int getMem_cod() {
        return mem_cod;
    }

    public void setMem_cod(int mem_cod) {
        this.mem_cod = mem_cod;
    }

    public int getEsc_cod() {
        return esc_cod;
    }

    public void setEsc_cod(int esc_cod) {
        this.esc_cod = esc_cod;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Mensalidade> getAbertas() {
        return abertas;
    }

    public void setAbertas(List<Mensalidade> abertas) {
        this.abertas = abertas;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
