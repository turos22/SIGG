package com.example.sigg_backend.models;

import java.time.LocalDate;
import java.util.List;

import com.example.sigg_backend.db.dao.PresidenteDAO;
import com.example.sigg_backend.db.util.Conexao;

public class Presidente extends Escoteiro {
    private int pres_cod;
    private LocalDate gestao_ini;
    private LocalDate gestao_fim;

    public Presidente() {
    }

    @Override
    public String validar() {
        if (gestao_ini == null || gestao_fim == null)
            return "Início e fim da gestão são obrigatórios";
        if (gestao_fim.isBefore(gestao_ini))
            return "O fim da gestão não pode ser antes do início";
        return null;
    }

    @Override
    public boolean gravar(Conexao conexao) {
        return new PresidenteDAO().gravar(conexao, this);
    }

    @Override
    public Presidente buscar(Conexao conexao, int memCod) {
        return new PresidenteDAO().get(conexao, memCod);
    }

    @Override
    public boolean existe(Conexao conexao, int memCod) {
        return new PresidenteDAO().existe(conexao, memCod);
    }

    public List<Presidente> listarPresidentes(Conexao conexao) {
        return new PresidenteDAO().get(conexao);
    }

    public int getPres_cod() {
        return pres_cod;
    }

    public void setPres_cod(int pres_cod) {
        this.pres_cod = pres_cod;
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
}
