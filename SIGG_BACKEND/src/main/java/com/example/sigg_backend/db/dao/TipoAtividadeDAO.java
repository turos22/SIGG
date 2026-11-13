package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.models.TipoAtividade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoAtividadeDAO implements IDAO<TipoAtividade> {

    // Valida se já existe um tipo de atividade com o mesmo nome
    public boolean existeNome(Conexao conexao, String nome, int excetoCod) {
        ResultSet rs = conexao.consultar(
                "SELECT COUNT(*) AS total FROM tipo_atividade WHERE LOWER(atv_nome) = LOWER(?) AND atv_cod <> ?",
                nome.trim(), excetoCod
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
        tipo.setAtv_cod(novoCod);

        return conexao.manipular(
                "INSERT INTO tipo_atividade (atv_cod, atv_nome, atv_valor) VALUES (?, ?, ?)",
                tipo.getAtv_cod(),
                tipo.getAtv_nome().trim(),
                tipo.getAtv_valor()
        );
    }

    @Override
    public boolean alterar(Conexao conexao, TipoAtividade tipo) {
        return conexao.manipular(
                "UPDATE tipo_atividade SET atv_nome = ?, atv_valor = ? WHERE atv_cod = ?",
                tipo.getAtv_nome().trim(),
                tipo.getAtv_valor(),
                tipo.getAtv_cod()
        );
    }

    @Override
    public boolean apagar(Conexao conexao, TipoAtividade tipo) {
        return conexao.manipular(
                "DELETE FROM tipo_atividade WHERE atv_cod = ?",
                tipo.getAtv_cod()
        );
    }

    @Override
    public List<TipoAtividade> get(Conexao conexao) {
        return get(conexao, "");
    }

    @Override
    public List<TipoAtividade> get(Conexao conexao, String filtro) {
        String sql = "SELECT atv_cod, atv_nome, atv_valor FROM tipo_atividade";
        ResultSet rs;

        if (filtro != null && !filtro.trim().isEmpty()) {
            sql += " WHERE atv_nome ILIKE ? ORDER BY atv_cod";
            rs = conexao.consultar(sql, "%" + filtro.trim() + "%");
        } else {
            sql += " ORDER BY atv_cod";
            rs = conexao.consultar(sql);
        }

        List<TipoAtividade> lista = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                TipoAtividade tipo = new TipoAtividade(
                        rs.getInt("atv_cod"),
                        rs.getString("atv_nome"),
                        BigDecimal.valueOf(rs.getDouble("atv_valor")).setScale(2, RoundingMode.HALF_UP)
                );
                lista.add(tipo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public TipoAtividade get(Conexao conexao, int id) {
        ResultSet rs = conexao.consultar(
                "SELECT atv_cod, atv_nome, atv_valor FROM tipo_atividade WHERE atv_cod = ?",
                id
        );
        try {
            if (rs != null && rs.next()) {
                return new TipoAtividade(
                        rs.getInt("atv_cod"),
                        rs.getString("atv_nome"),
                        BigDecimal.valueOf(rs.getDouble("atv_valor")).setScale(2, RoundingMode.HALF_UP)
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}