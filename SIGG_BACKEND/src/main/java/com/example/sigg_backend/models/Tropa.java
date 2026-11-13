package com.example.sigg_backend.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import com.example.sigg_backend.db.dao.TropaDAO;
import com.example.sigg_backend.db.util.Conexao;

public class Tropa {
    private int cod;
    private String nome;

    // Sem isto o Jackson usa o construtor de 2 argumentos e recusa o corpo sem "cod" (cadastro novo)
    @JsonCreator
    public Tropa(){}

    public Tropa(int cod, String nome) {
        this.cod = cod;
        this.nome = nome;
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

    public void normalizar() {
        if (nome != null)
            nome = nome.trim();
    }

    public String validar() {
        if (nome == null || nome.isBlank() || nome.length() > 20)
            return "Nome da tropa é obrigatório (até 20 caracteres)";
        if (nome.chars().anyMatch(Character::isDigit))
            return "Nome não pode conter números";
        return null;
    }

    public List<Tropa> listar(Conexao conexao) {
        return new TropaDAO().get(conexao);
    }

    public Tropa buscar(Conexao conexao, int cod) {
        return new TropaDAO().get(conexao, cod);
    }

    public boolean nomeJaUsadoPorOutra(Conexao conexao) {
        return new TropaDAO().existeNome(conexao, nome, cod);
    }

    public boolean temDependentes(Conexao conexao) {
        return new TropaDAO().temDependentes(conexao, cod);
    }

    public boolean gravar(Conexao conexao) {
        return new TropaDAO().gravar(conexao, this);
    }

    public boolean alterar(Conexao conexao) {
        return new TropaDAO().alterar(conexao, this);
    }

    public boolean apagar(Conexao conexao) {
        return new TropaDAO().apagar(conexao, this);
    }
}
