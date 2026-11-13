package com.example.sigg_backend.models;

import com.example.sigg_backend.db.dao.CategoriaMensalidadeDAO;
import java.math.BigDecimal;
import java.util.List;
import com.example.sigg_backend.db.util.Conexao;

public class CategoriaMensalidade {
    private Integer cat_mens_cod;
    private String descricao;
    private BigDecimal valor;
    private int qtd_irmao;

    public CategoriaMensalidade() {
    }

    public CategoriaMensalidade(Integer cat_mens_cod, String descricao, BigDecimal valor, int qtd_irmao) {
        this.cat_mens_cod = cat_mens_cod;
        this.descricao = descricao;
        this.valor = valor;
        this.qtd_irmao = qtd_irmao;
    }

    public boolean gravar(Conexao conexao) {
        return new CategoriaMensalidadeDAO().gravar(conexao, this);
    }

    public boolean alterar(Conexao conexao) {
        return new CategoriaMensalidadeDAO().alterar(conexao, this);
    }

    public boolean apagar(Conexao conexao) {
        return new CategoriaMensalidadeDAO().apagar(conexao, this);
    }

    public static CategoriaMensalidade buscarPorId(Conexao conexao, int id) {
        return new CategoriaMensalidadeDAO().get(conexao, id);
    }
    public CategoriaMensalidade buscar(Conexao conexao, int id) {
        return buscarPorId(conexao, id);
    }
    
    public static List<CategoriaMensalidade> listar(Conexao conexao, String filtro) {
        return new CategoriaMensalidadeDAO().get(conexao, filtro);
    }

    public static boolean descricaoJaExiste(Conexao conexao, String descricao, int excetoCod) {
        return new CategoriaMensalidadeDAO().existeDescricao(conexao, descricao, excetoCod);
    }

    public Integer getCat_mens_cod() {
        return cat_mens_cod;
    }

    public void setCat_mens_cod(Integer cat_mens_cod) {
        this.cat_mens_cod = cat_mens_cod;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public int getQtd_irmao() {
        return qtd_irmao;
    }

    public void setQtd_irmao(int qtd_irmao) {
        this.qtd_irmao = qtd_irmao;
    }
}