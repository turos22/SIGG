package com.example.sigg_backend.models;

import java.time.LocalDate;
import java.util.List;

import com.example.sigg_backend.db.dao.ContribuicaoDAO;

public class Contribuicao {
    private int con_cod;
    private LocalDate con_dt;
    private LocalDate con_dt_fim;
    private int anjo;
    private int adotado;
    private String nome_anjo;
    private String nome_adotado;

    public Contribuicao() {
    }

    public String validar() {
        if (anjo <= 0 || adotado <= 0)
            return "Anjo e adotado são obrigatórios";
        if (anjo == adotado)
            return "O Anjo não pode apadrinhar a si mesmo";
        return null;
    }

    public boolean gravar() {
        return new ContribuicaoDAO().gravar(this);
    }

    public List<Contribuicao> listar() {
        return new ContribuicaoDAO().get();
    }

    public List<Contribuicao> doAnjo(int escCodAnjo) {
        return new ContribuicaoDAO().doAnjo(escCodAnjo);
    }

    public int getCon_cod() {
        return con_cod;
    }

    public void setCon_cod(int con_cod) {
        this.con_cod = con_cod;
    }

    public LocalDate getCon_dt() {
        return con_dt;
    }

    public void setCon_dt(LocalDate con_dt) {
        this.con_dt = con_dt;
    }

    public LocalDate getCon_dt_fim() {
        return con_dt_fim;
    }

    public void setCon_dt_fim(LocalDate con_dt_fim) {
        this.con_dt_fim = con_dt_fim;
    }

    public int getAnjo() {
        return anjo;
    }

    public void setAnjo(int anjo) {
        this.anjo = anjo;
    }

    public int getAdotado() {
        return adotado;
    }

    public void setAdotado(int adotado) {
        this.adotado = adotado;
    }

    public String getNome_anjo() {
        return nome_anjo;
    }

    public void setNome_anjo(String nome_anjo) {
        this.nome_anjo = nome_anjo;
    }

    public String getNome_adotado() {
        return nome_adotado;
    }

    public void setNome_adotado(String nome_adotado) {
        this.nome_adotado = nome_adotado;
    }
}
