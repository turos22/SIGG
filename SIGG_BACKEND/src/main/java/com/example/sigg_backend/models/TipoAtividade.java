package com.example.sigg_backend.models;

import java.math.BigDecimal;
import java.util.List;
import com.example.sigg_backend.db.dao.TipoAtividadeDAO;
import com.example.sigg_backend.db.util.Conexao;

public class TipoAtividade {
    private Integer atv_cod;
    private String atv_nome;
    private BigDecimal atv_valor;

    public TipoAtividade() {
    }

    public TipoAtividade(int atv_cod, String atv_nome, BigDecimal atv_valor) {
        this.atv_cod = atv_cod;
        this.atv_nome = atv_nome;
        this.atv_valor = atv_valor;
    }

    // Métodos de negócio / persistência (Model chama o DAO)
    public boolean gravar(Conexao conexao) {
        return new TipoAtividadeDAO().gravar(conexao, this);
    }

    public boolean alterar(Conexao conexao) {
        return new TipoAtividadeDAO().alterar(conexao, this);
    }

    public boolean apagar(Conexao conexao) {
        return new TipoAtividadeDAO().apagar(conexao, this);
    }

    public static TipoAtividade buscarPorId(Conexao conexao, int id) {
        return new TipoAtividadeDAO().get(conexao, id);
    }

    public static List<TipoAtividade> listar(Conexao conexao, String filtro) {
        return new TipoAtividadeDAO().get(conexao, filtro);
    }

    public static boolean existeNome(Conexao conexao, String nome, int excetoCod) {
        return new TipoAtividadeDAO().existeNome(conexao, nome, excetoCod);
    }

    // Getters e Setters
    public int getAtv_cod() {
        return atv_cod;
    }

    public void setAtv_cod(int atv_cod) {
        this.atv_cod = atv_cod;
    }

    public String getAtv_nome() {
        return atv_nome;
    }

    public void setAtv_nome(String atv_nome) {
        this.atv_nome = atv_nome;
    }

    public BigDecimal getAtv_valor() {
        return atv_valor;
    }

    public void setAtv_valor(BigDecimal atv_valor) {
        this.atv_valor = atv_valor;
    }
}