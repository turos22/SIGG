package com.example.sigg_backend.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.models.Tropa;

public class TropaDAO implements IDAO<Tropa> {

    @Override
    public boolean gravar(Conexao conexao, Tropa tropa){
        int ultimo = conexao.getMaxPK("tropa", "tro_cod");
        if (ultimo < 0)
            return false;
        tropa.setCod(ultimo + 1);
        return conexao.manipular("INSERT INTO tropa (tro_cod, tro_nome) VALUES (?, ?)", tropa.getCod(), tropa.getNome());
    }

    @Override
    public boolean alterar(Conexao conexao, Tropa tropa){
        return conexao.manipular("UPDATE tropa SET tro_nome = ? WHERE tro_cod = ?", tropa.getNome(), tropa.getCod());
    }

    @Override
    public boolean apagar(Conexao conexao, Tropa tropa){
        return conexao.manipular("DELETE FROM tropa WHERE tro_cod = ?", tropa.getCod());
    }

    @Override
    public List<Tropa> get(Conexao conexao){
        return buscar(conexao, "");
    }

    @Override
    public List<Tropa> get(Conexao conexao, String filtro){
        return buscar(conexao, "WHERE tro_nome ILIKE ?", "%" + filtro + "%");
    }

    @Override
    public Tropa get(Conexao conexao, int id){
        List<Tropa> tropas = buscar(conexao, "WHERE tro_cod = ?", id);
        return tropas.isEmpty() ? null : tropas.get(0);
    }

    public boolean existeNome(Conexao conexao, String nome, int exceto){
        return existe(conexao, "SELECT 1 FROM tropa WHERE LOWER(tro_nome) = LOWER(?) AND tro_cod <> ?", nome, exceto);
    }

    // Patrulha ou chefe (mesmo com gestão encerrada: o histórico mantém a FK) impedem a exclusão
    public boolean temDependentes(Conexao conexao, int troCod){
        return existe(conexao, "SELECT 1 FROM patrulha WHERE tropa_tro_cod = ?"
                + " UNION SELECT 1 FROM chefe WHERE tropa_tro_cod = ?", troCod, troCod);
    }

    private boolean existe(Conexao conexao, String sql, Object... valores){
        ResultSet rs = conexao.consultar(sql, valores);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    private List<Tropa> buscar(Conexao conexao, String condicao, Object... valores){
        ResultSet rs = conexao.consultar("SELECT tro_cod, tro_nome FROM tropa " + condicao + " ORDER BY tro_nome", valores);
        List<Tropa> tropas = new ArrayList<>();
        try {
            while (rs != null && rs.next())
                tropas.add(new Tropa(rs.getInt("tro_cod"), rs.getString("tro_nome")));
        } catch (SQLException sqlex) {
            tropas.clear();
        }
        return tropas;
    }
}
