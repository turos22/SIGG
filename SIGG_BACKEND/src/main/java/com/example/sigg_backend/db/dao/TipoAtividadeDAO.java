package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.models.TipoAtividade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoAtividadeDAO implements IDAO<TipoAtividade> {

    // Valida se já existe um tipo de atividade com o mesmo nome
    public boolean existeNome(Conexao conexao, String nome, int excetoCod) {
        ResultSet rs = conexao.consultar(
                "SELECT COUNT(*) AS total FROM tipo_atividade WHERE LOWER(ta_desc) = LOWER(?) AND ta_cod <> ?",
                descricao.trim(), excetoCod
        );
        try {
            if (rs != null && rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean gravar(Conexao conexao, TipoAtividade tipo) {
        int ultimo = conexao.getMaxPK("tipo_atividade", "atv_cod");
        int novoCod = (ultimo < 0) ? 1 : ultimo + 1;
        tipo.setTa_cod(novoCod);

        return conexao.manipular(
                "INSERT INTO tipo_atividade (ta_cod, ta_desc) VALUES (?, ?)",
                tipo.getTa_cod(),
                tipo.getTa_desc().trim()
        );
    }

    @Override
    public boolean alterar(Conexao conexao, TipoAtividade tipo) {
        return conexao.manipular(
                "UPDATE tipo_atividade SET ta_desc = ? WHERE ta_cod = ?",
                tipo.getTa_desc().trim(),
                tipo.getTa_cod()
        );
    }

    @Override
    public boolean apagar(Conexao conexao, TipoAtividade tipo) {
        return conexao.manipular(
                "DELETE FROM tipo_atividade WHERE ta_cod = ?",
                tipo.getTa_cod()
        );
    }

    @Override
    public List<TipoAtividade> get(Conexao conexao) {
        return get(conexao, "");
    }

    @Override
    public List<TipoAtividade> get(Conexao conexao, String filtro) {
        String sql = "SELECT ta_cod, ta_desc FROM tipo_atividade";
        ResultSet rs;

        if (filtro != null && !filtro.trim().isEmpty()) {
            sql += " WHERE ta_desc ILIKE ? ORDER BY ta_cod";
            rs = conexao.consultar(sql, "%" + filtro.trim() + "%");
        } else {
            sql += " ORDER BY ta_cod";
            rs = conexao.consultar(sql);
        }

        List<TipoAtividade> lista = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                lista.add(montar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public TipoAtividade get(Conexao conexao, int id) {
        ResultSet rs = conexao.consultar(
                 "SELECT ta_cod, ta_desc FROM tipo_atividade WHERE ta_cod = ?",
                id
        );
        try {
            if (rs != null && rs.next()) {
                return montar(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private TipoAtividade montar(ResultSet rs) throws SQLException {
        TipoAtividade tipo = new TipoAtividade();
        tipo.setTa_cod(rs.getInt("ta_cod"));
        tipo.setTa_desc(rs.getString("ta_desc"));
        return tipo;
    }
}
