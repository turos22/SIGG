package com.example.sigg_backend.models;

public class Patrulha {
    private int cod;
    private String nome;
    private Escoteiro lider;
    private Escoteiro sublider;
    private Tropa tropa;

    public Patrulha(){}

    public Patrulha(int cod, String nome, Escoteiro lider, Escoteiro sublider, Tropa tropa) {
        this.cod = cod;
        this.nome = nome;
        this.lider = lider;
        this.sublider = sublider;
        this.tropa = tropa;
    }

    public int getCod() {
        return cod;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Escoteiro getLider() {
        return lider;
    }

    public void setLider(Escoteiro lider) {
        this.lider = lider;
    }

    public Escoteiro getSublider() {
        return sublider;
    }

    public void setSublider(Escoteiro sublider) {
        this.sublider = sublider;
    }

    public Tropa getTropa() {
        return tropa;
    }

    public void setTropa(Tropa tropa) {
        this.tropa = tropa;
    }
}
