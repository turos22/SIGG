package com.example.sigg_backend.models;

import java.math.BigDecimal;
import java.util.List;
import com.example.sigg_backend.db.dao.TipoAtividadeDAO;
import com.example.sigg_backend.db.util.Conexao;

// Tipo de atividade (tabela tipo_atividade: ta_cod, ta_desc). O valor cobrado fica em atividade_especial.
public class TipoAtividade {
    private int ta_cod;
    private String ta_desc;

    // Só o construtor vazio + setters: assim o JSON do cliente pode omitir ta_cod (o DAO gera o código)
    public TipoAtividade() {
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
