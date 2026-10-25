package com.example.sigg_backend.db.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.springframework.jdbc.datasource.DataSourceUtils;

public class Conexao {

    private final DataSource dataSource;
    private final ThreadLocal<String> erro = ThreadLocal.withInitial(() -> "");

    Conexao(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    public String getMensagemErro() {
        return erro.get();
    }

    public boolean getEstadoConexao() {
        erro.set("");
        Connection connect = null;
        try {
            connect = DataSourceUtils.getConnection(dataSource);
            if (connect.isValid(2))
                return true;
            erro.set("Impossivel conectar com a base de dados: o banco não respondeu");
            return false;
        } catch (Exception ex) {
            erro.set("Impossivel conectar com a base de dados: " + ex.getMessage());
            return false;
        } finally {
            if (connect != null)
                DataSourceUtils.releaseConnection(connect, dataSource);
        }
    }

    public boolean manipular(String sql, Object... parametros) {
        erro.set("");
        Connection connect = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = preparar(connect, sql, parametros)) {
            return statement.executeUpdate() >= 1;
        } catch (SQLException sqlex) {
            erro.set("Erro: " + sqlex.getMessage());
            return false;
        } finally {
            DataSourceUtils.releaseConnection(connect, dataSource);
        }
    }
    public ResultSet consultar(String sql, Object... parametros) {
        erro.set("");
        Connection connect = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = preparar(connect, sql, parametros);
             ResultSet rs = statement.executeQuery()) {
            CachedRowSet copia = RowSetProvider.newFactory().createCachedRowSet();
            copia.populate(rs);
            return copia;
        } catch (SQLException sqlex) {
            erro.set("Erro: " + sqlex.getMessage());
            return null;
        } finally {
            DataSourceUtils.releaseConnection(connect, dataSource);
        }
    }

    public int getMaxPK(String tabela, String chave) {
        ResultSet rs = consultar("select max(" + chave + ") from " + tabela);
        try {
            return rs != null && rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException sqlex) {
            erro.set("Erro: " + sqlex.getMessage());
            return -1;
        }
    }
    private PreparedStatement preparar(Connection connect, String sql, Object... parametros) throws SQLException {
        PreparedStatement statement = connect.prepareStatement(sql);
        for (int i = 0; i < parametros.length; i++)
            statement.setObject(i + 1, parametros[i]);
        return statement;
    }
}