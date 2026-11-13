package com.example.sigg_backend.models;

import java.util.List;
import com.example.sigg_backend.db.dao.TipoAtividadeDAO;

// Tipo de atividade (tabela tipo_atividade: ta_cod, ta_desc). O valor cobrado fica em atividade_especial.
public class TipoAtividade {
    private int ta_cod;
    private String ta_desc;

    // Só o construtor vazio + setters: assim o JSON do cliente pode omitir ta_cod (o DAO gera o código)
    public TipoAtividade() {
    }

    // Métodos de negócio / persistência (Model chama o DAO)
    public boolean gravar() {
        return new TipoAtividadeDAO().gravar(this);
    }

    public boolean alterar() {
        return new TipoAtividadeDAO().alterar(this);
    }

    public boolean apagar() {
        return new TipoAtividadeDAO().apagar(this);
    }

    public static TipoAtividade buscarPorId(int id) {
        return new TipoAtividadeDAO().get(id);
    }

    public static List<TipoAtividade> listar(String filtro) {
        return new TipoAtividadeDAO().get(filtro);
    }

    public static boolean existeDescricao(String descricao, int excetoCod) {
        return new TipoAtividadeDAO().existeDescricao(descricao, excetoCod);
    }

    // Getters e Setters
    public int getTa_cod() {
        return ta_cod;
    }

    public void setTa_cod(int ta_cod) {
        this.ta_cod = ta_cod;
    }

    public String getTa_desc() {
        return ta_desc;
    }

    public void setTa_desc(String ta_desc) {
        this.ta_desc = ta_desc;
    }
}
