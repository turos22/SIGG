package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.CategoriaMensalidade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaMensalidadeDAO implements IDAO<CategoriaMensalidade> {

    public boolean existeDescricao(String descricao, int excetoCod) {
        Conexao conexao = SingletonDB.getConexao();
        ResultSet rs = conexao.consultar(
                "SELECT COUNT(*) AS total FROM categoria_mensalidade WHERE LOWER(cat_mens_desc) = LOWER(?) AND cat_mens_cod <> ?",
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
    public boolean gravar(CategoriaMensalidade categoria) {
        Conexao conexao = SingletonDB.getConexao();
        int ultimo = conexao.getMaxPK("categoria_mensalidade", "cat_mens_cod");
        int novoCod = (ultimo < 0) ? 1 : ultimo + 1;
        categoria.setCat_mens_cod(novoCod);

        return conexao.manipular(
                "INSERT INTO categoria_mensalidade (cat_mens_cod, cat_mens_desc, cat_mens_valor, cat_mens_qtd_irmao) VALUES (?, ?, ?, ?)",
                categoria.getCat_mens_cod(),
                categoria.getDescricao().trim(),
                categoria.getValor(),
                categoria.getQtd_irmao()
        );
    }

    @Override
    public boolean alterar(CategoriaMensalidade categoria) {
        Conexao conexao = SingletonDB.getConexao();
        return conexao.manipular(
                "UPDATE categoria_mensalidade SET cat_mens_desc = ?, cat_mens_valor = ?, cat_mens_qtd_irmao = ? WHERE cat_mens_cod = ?",
                categoria.getDescricao().trim(),
                categoria.getValor(),
                categoria.getQtd_irmao(),
                categoria.getCat_mens_cod()
        );
    }

    @Override
    public boolean apagar(CategoriaMensalidade categoria) {
        Conexao conexao = SingletonDB.getConexao();
        return conexao.manipular(
                "DELETE FROM categoria_mensalidade WHERE cat_mens_cod = ?",
                categoria.getCat_mens_cod()
        );
    }

    @Override
    public List<CategoriaMensalidade> get() {
        return get("");
    }

    @Override
    public List<CategoriaMensalidade> get(String filtro) {
        String sql = "SELECT cat_mens_cod, cat_mens_desc, cat_mens_valor, cat_mens_qtd_irmao FROM categoria_mensalidade";
        ResultSet rs;

        if (filtro != null && !filtro.trim().isEmpty()) {
            sql += " WHERE cat_mens_desc ILIKE ? ORDER BY cat_mens_cod";
            rs = SingletonDB.getConexao().consultar(sql, "%" + filtro.trim() + "%");
        } else {
            sql += " ORDER BY cat_mens_cod";
            rs = SingletonDB.getConexao().consultar(sql);
        }

        List<CategoriaMensalidade> lista = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                CategoriaMensalidade cat = new CategoriaMensalidade(
                        rs.getInt("cat_mens_cod"),
                        rs.getString("cat_mens_desc"),
                        BigDecimal.valueOf(rs.getDouble("cat_mens_valor")).setScale(2, RoundingMode.HALF_UP),
                        rs.getInt("cat_mens_qtd_irmao")
                );
                lista.add(cat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public CategoriaMensalidade get(int id) {
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT cat_mens_cod, cat_mens_desc, cat_mens_valor, cat_mens_qtd_irmao FROM categoria_mensalidade WHERE cat_mens_cod = ?",
                id
        );
        try {
            if (rs != null && rs.next()) {
                return new CategoriaMensalidade(
                        rs.getInt("cat_mens_cod"),
                        rs.getString("cat_mens_desc"),
                        BigDecimal.valueOf(rs.getDouble("cat_mens_valor")).setScale(2, RoundingMode.HALF_UP),
                        rs.getInt("cat_mens_qtd_irmao")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}