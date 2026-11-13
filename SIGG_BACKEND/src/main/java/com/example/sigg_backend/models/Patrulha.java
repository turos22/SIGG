package com.example.sigg_backend.models;

import java.util.List;

import com.example.sigg_backend.db.dao.PatrulhaDAO;
import com.example.sigg_backend.db.util.Conexao;
import com.fasterxml.jackson.annotation.JsonCreator;

public class Patrulha {
    private int cod;
    private String nome;
    private Escoteiro lider;
    private Escoteiro sublider;
    private Tropa tropa;

    // Sem isto o Jackson usa o construtor de 5 argumentos e recusa o corpo sem "cod" (cadastro novo)
    @JsonCreator
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

    // Tira espaços do nome e trata líder/sublíder sem código como ausentes
    public void normalizar() {
        if (nome != null)
            nome = nome.trim();
        if (lider != null && lider.getEsc_cod() <= 0)
            lider = null;
        if (sublider != null && sublider.getEsc_cod() <= 0)
            sublider = null;
    }

    public String validar() {
        if (nome == null || nome.isBlank() || nome.length() > 20)
            return "Nome da patrulha é obrigatório (até 20 caracteres)";
        if (nome.chars().anyMatch(Character::isDigit))
            return "Nome não pode conter números";
        if (tropa == null || tropa.getCod() <= 0)
            return "Tropa é obrigatória";
        if (lider != null && sublider != null && lider.getEsc_cod() == sublider.getEsc_cod())
            return "Líder e sublíder devem ser pessoas diferentes";
        return null;
    }

    public List<Patrulha> listar(Conexao conexao, Integer troCod) {
        return new PatrulhaDAO().listar(conexao, troCod);
    }

    public Patrulha buscar(Conexao conexao, int cod) {
        return new PatrulhaDAO().get(conexao, cod);
    }

    public boolean escoteiroEhDaTropa(Conexao conexao, int escCod) {
        return new PatrulhaDAO().escoteiroNaTropa(conexao, escCod, tropa.getCod());
    }

    public boolean temEscoteiros(Conexao conexao) {
        return new PatrulhaDAO().temEscoteiros(conexao, cod);
    }

    public boolean gravar(Conexao conexao) {
        return new PatrulhaDAO().gravar(conexao, this);
    }

    public boolean alterar(Conexao conexao) {
        return new PatrulhaDAO().alterar(conexao, this);
    }

    public boolean apagar(Conexao conexao) {
        return new PatrulhaDAO().apagar(conexao, this);
    }
}
