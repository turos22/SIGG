package com.example.sigg_backend.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.Parametrizacao;

// A tabela parametrizacao tem uma linha só (par_cod = 1): não há cadastro nem exclusão, só leitura e alteração.
public class ParametrizacaoDAO {

    private static final int LINHA_UNICA = 1;

    public Parametrizacao get() {
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT par_sigla, par_nome_sistema, par_slogan, par_nome_grupo, par_endereco, par_cidade,"
                        + " par_estado, par_telefone, par_email, par_logo FROM parametrizacao WHERE par_cod = ?",
                LINHA_UNICA);
        try {
            if (rs != null && rs.next()) {
                Parametrizacao p = new Parametrizacao();
                p.setSigla(rs.getString("par_sigla"));
                p.setNomeSistema(rs.getString("par_nome_sistema"));
                p.setSlogan(rs.getString("par_slogan"));
                p.setNomeGrupo(rs.getString("par_nome_grupo"));
                p.setEndereco(rs.getString("par_endereco"));
                p.setCidade(rs.getString("par_cidade"));
                p.setEstado(rs.getString("par_estado"));
                p.setTelefone(rs.getString("par_telefone"));
                p.setEmail(rs.getString("par_email"));
                p.setLogo(rs.getString("par_logo"));
                return p;
            }
        } catch (SQLException sqlex) {
            return null;
        }
        return null;
    }

    public boolean alterar(Parametrizacao p) {
        return SingletonDB.getConexao().manipular(
                "UPDATE parametrizacao SET par_sigla = ?, par_nome_sistema = ?, par_slogan = ?, par_nome_grupo = ?,"
                        + " par_endereco = ?, par_cidade = ?, par_estado = ?, par_telefone = ?, par_email = ?,"
                        + " par_logo = ? WHERE par_cod = ?",
                p.getSigla(), p.getNomeSistema(), p.getSlogan(), p.getNomeGrupo(), p.getEndereco(), p.getCidade(),
                p.getEstado(), p.getTelefone(), p.getEmail(), p.getLogo(), LINHA_UNICA);
    }
}
