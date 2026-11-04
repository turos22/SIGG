package com.example.sigg_backend.models;

import java.util.ArrayList;
import java.util.List;

import com.example.sigg_backend.db.dao.EscoteiroDAO;

public class Escoteiro {
    private Membro membro;
    private int esc_cod;
    private int cat_mensalidade;
    private int anjo;
    private Patrulha patrulha;
    private int esc_sit_fin;
    private List<Responsavel> responsaveis = new ArrayList<>();

    public Escoteiro() {
    }

    public String validar() {
        if (cat_mensalidade < 1 || cat_mensalidade > 5)
            return "Categoria de mensalidade deve ser de 1 a 5";
        if (patrulha == null || patrulha.getCod() <= 0)
            return "Patrulha é obrigatória";
        return null;
    }

    public boolean gravar() {
        return new EscoteiroDAO().gravar(this);
    }

    public List<Escoteiro> listar() {
        return new EscoteiroDAO().get();
    }

    public Escoteiro buscar(int memCod) {
        return new EscoteiroDAO().get(memCod);
    }

    public boolean existe(int memCod) {
        return new EscoteiroDAO().existe(memCod);
    }

    public Membro getMembro() {
        return membro;
    }

    public void setMembro(Membro membro) {
        this.membro = membro;
    }

    public int getEsc_cod() {
        return esc_cod;
    }

    public void setEsc_cod(int esc_cod) {
        this.esc_cod = esc_cod;
    }

    public int getCat_mensalidade() {
        return cat_mensalidade;
    }

    public void setCat_mensalidade(int cat_mensalidade) {
        this.cat_mensalidade = cat_mensalidade;
    }

    public int getAnjo() {
        return anjo;
    }

    public void setAnjo(int anjo) {
        this.anjo = anjo;
    }

    public Patrulha getPatrulha() {
        return patrulha;
    }

    public void setPatrulha(Patrulha patrulha) {
        this.patrulha = patrulha;
    }

    public int getEsc_sit_fin() {
        return esc_sit_fin;
    }

    public void setEsc_sit_fin(int esc_sit_fin) {
        this.esc_sit_fin = esc_sit_fin;
    }

    public List<Responsavel> getResponsaveis() {
        return responsaveis;
    }

    public void setResponsaveis(List<Responsavel> responsaveis) {
        this.responsaveis = responsaveis;
    }
}
