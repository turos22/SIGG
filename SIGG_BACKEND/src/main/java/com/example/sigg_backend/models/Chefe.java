package com.example.sigg_backend.models;

import java.time.LocalDate;
import java.util.List;

import com.example.sigg_backend.db.dao.ChefeDAO;
import com.example.sigg_backend.db.util.Conexao;

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
    public boolean gravar(Conexao conexao) {
        return new ChefeDAO().gravar(conexao, this);
    }

    @Override
    public Chefe buscar(Conexao conexao, int memCod) {
        return new ChefeDAO().get(conexao, memCod);
    }

    @Override
    public boolean existe(Conexao conexao, int memCod) {
        return new ChefeDAO().existe(conexao, memCod);
    }

    // A gestão vale até o último dia (inclusive), como no login e na lista de membros
    public boolean estaAtivo() {
        return gestao_fim == null || !gestao_fim.isBefore(LocalDate.now());
    }

    public Integer tropaAtiva(Conexao conexao, int memCod) {
        return new ChefeDAO().tropaAtiva(conexao, memCod);
    }

    public boolean reativar(Conexao conexao) {
        return new ChefeDAO().reativar(conexao, this);
    }

    public boolean encerrar(Conexao conexao, LocalDate fim) {
        return new ChefeDAO().encerrar(conexao, getMembro().getMem_cod(), fim);
    }

    public List<Chefe> listarChefes(Conexao conexao) {
        return new ChefeDAO().get(conexao);
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
