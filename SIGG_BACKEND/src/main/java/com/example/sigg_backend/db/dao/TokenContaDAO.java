package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.TokenConta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TokenContaDAO implements IDAO<TokenConta> {

    @Override
    public boolean gravar(TokenConta token){
        Conexao conexao = SingletonDB.getConexao();
        int ultimo = conexao.getMaxPK("conta_token", "tok_cod");
        if (ultimo < 0)
            return false;
        token.setTok_cod(ultimo + 1);
        return conexao.manipular("INSERT INTO conta_token (tok_cod, cta_cod, tok_hash, tok_tipo, tok_expira)"
                        + " VALUES (?, ?, ?, ?, ?)",
                token.getTok_cod(), token.getCta_cod(), token.hashDoToken(), token.getTipo(), token.getExpira());
    }

    @Override
    public boolean alterar(TokenConta token){
        return SingletonDB.getConexao().manipular("UPDATE conta_token SET tok_usado_em = ? WHERE tok_cod = ?",
                token.getUsado_em(), token.getTok_cod());
    }

    @Override
    public boolean apagar(TokenConta token){
        return SingletonDB.getConexao().manipular("DELETE FROM conta_token WHERE tok_cod = ?", token.getTok_cod());
    }

    @Override
    public List<TokenConta> get(){
        return buscar("");
    }

    @Override
    public List<TokenConta> get(String filtro){
        throw new UnsupportedOperationException("Filtro de token não implementado");
    }

    @Override
    public TokenConta get(int id){
        List<TokenConta> tokens = buscar("WHERE t.tok_cod = ?", id);
        return tokens.isEmpty() ? null : tokens.get(0);
    }

    public TokenConta doHash(String hash, String tipo){
        List<TokenConta> tokens = buscar("WHERE t.tok_hash = ? AND t.tok_tipo = ?", hash, tipo);
        return tokens.isEmpty() ? null : tokens.get(0);
    }

    public boolean invalidarAnteriores(int ctaCod, String tipo, LocalDateTime agora){
        return SingletonDB.getConexao().manipular("UPDATE conta_token SET tok_usado_em = ?"
                + " WHERE cta_cod = ? AND tok_tipo = ? AND tok_usado_em IS NULL", agora, ctaCod, tipo);
    }

    private TokenConta preencher(ResultSet rs) throws SQLException {
        TokenConta token = new TokenConta();
        token.setTok_cod(rs.getInt("tok_cod"));
        token.setCta_cod(rs.getInt("cta_cod"));
        token.definirHash(rs.getString("tok_hash"));
        token.setTipo(rs.getString("tok_tipo"));
        Timestamp expira = rs.getTimestamp("tok_expira");
        token.setExpira(expira == null ? null : expira.toLocalDateTime());
        Timestamp usado = rs.getTimestamp("tok_usado_em");
        token.setUsado_em(usado == null ? null : usado.toLocalDateTime());
        return token;
    }

    private List<TokenConta> buscar(String condicao, Object... valores){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT t.* FROM conta_token t " + condicao + " ORDER BY t.tok_cod", valores);
        List<TokenConta> tokens = new ArrayList<>();
        try {
            while (rs != null && rs.next())
                tokens.add(preencher(rs));
        } catch (SQLException sqlex) {
            tokens.clear();
        }
        return tokens;
    }
}
