package com.example.sigg_backend.models;

import java.time.LocalDate;
import java.util.List;

import com.example.sigg_backend.db.dao.ChefeDAO;

public class Chefe extends Escoteiro {
    private int chefe_cod;
    private LocalDate gestao_ini;
    private LocalDate gestao_fim;
    private Tropa tropa;

    public Chefe() {
    }

    @Override
    public String validar() {
        if (gestao_ini == null)
            return "Início da gestão é obrigatório";
        if (gestao_fim != null && gestao_fim.isBefore(gestao_ini))
            return "O fim da gestão não pode ser antes do início";
        if (tropa == null || tropa.getCod() <= 0)
            return "Tropa é obrigatória";
        return null;
    }

    @Override
    public boolean gravar() {
        return new ChefeDAO().gravar(this);
    }

    @Override
    public Chefe buscar(int memCod) {
        return new ChefeDAO().get(memCod);
    }

    @Override
    public boolean existe(int memCod) {
        return new ChefeDAO().existe(memCod);
    }

    public List<Chefe> listarChefes() {
        return new ChefeDAO().get();
    }

    public int getChefe_cod() {
        return chefe_cod;
    }

    public void setChefe_cod(int chefe_cod) {
        this.chefe_cod = chefe_cod;
    }

    public LocalDate getGestao_ini() {
        return gestao_ini;
    }

    public void setGestao_ini(LocalDate gestao_ini) {
        this.gestao_ini = gestao_ini;
    }

    public LocalDate getGestao_fim() {
        return gestao_fim;
    }

    public void setGestao_fim(LocalDate gestao_fim) {
        this.gestao_fim = gestao_fim;
    }

    public Tropa getTropa() {
        return tropa;
    }

    public void setTropa(Tropa tropa) {
        this.tropa = tropa;
    }
}
