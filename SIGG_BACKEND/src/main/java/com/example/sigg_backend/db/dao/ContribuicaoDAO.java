package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.Contribuicao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContribuicaoDAO implements IDAO<Contribuicao> {

    @Override
    public boolean gravar(Contribuicao contribuicao){
        Conexao conexao = SingletonDB.getConexao();
        int ultimo = conexao.getMaxPK("contribuicao", "con_cod");
        if (ultimo < 0)
            return false;
        contribuicao.setCon_cod(ultimo + 1);
        if (contribuicao.getCon_dt() == null)
            contribuicao.setCon_dt(LocalDate.now());
        return conexao.manipular("INSERT INTO contribuicao (con_cod, con_dt, con_dt_fim, anjo_esc_cod, adotado_esc_cod)"
                        + " VALUES (?, ?, ?, ?, ?)",
                contribuicao.getCon_cod(), contribuicao.getCon_dt(), contribuicao.getCon_dt_fim(),
                contribuicao.getAnjo(), contribuicao.getAdotado());
    }

    @Override
    public boolean alterar(Contribuicao contribuicao){
        return SingletonDB.getConexao().manipular("UPDATE contribuicao SET con_dt_fim = ? WHERE con_cod = ?",
                contribuicao.getCon_dt_fim(), contribuicao.getCon_cod());
    }

    @Override
    public boolean apagar(Contribuicao contribuicao){
        return SingletonDB.getConexao().manipular("UPDATE contribuicao SET con_dt_fim = CURRENT_DATE WHERE con_cod = ?",
                contribuicao.getCon_cod());
    }

    @Override
    public List<Contribuicao> get(){
        return buscar("");
    }

    @Override
    public List<Contribuicao> get(String filtro){
        return buscar("AND (ma.mem_nome ILIKE ? OR mo.mem_nome ILIKE ?)", "%" + filtro + "%", "%" + filtro + "%");
    }

    @Override
    public Contribuicao get(int id){
        List<Contribuicao> contribuicoes = buscar("AND c.con_cod = ?", id);
        return contribuicoes.isEmpty() ? null : contribuicoes.get(0);
    }

    public List<Contribuicao> doAnjo(int escCodAnjo){
        return buscar("AND c.anjo_esc_cod = ?", escCodAnjo);
    }

    private Contribuicao preencher(ResultSet rs) throws SQLException {
        Contribuicao contribuicao = new Contribuicao();
        contribuicao.setCon_cod(rs.getInt("con_cod"));
        contribuicao.setCon_dt(Datas.ler(rs, "con_dt"));
        contribuicao.setCon_dt_fim(Datas.ler(rs, "con_dt_fim"));
        contribuicao.setAnjo(rs.getInt("anjo_esc_cod"));
        contribuicao.setAdotado(rs.getInt("adotado_esc_cod"));
        contribuicao.setNome_anjo(rs.getString("nome_anjo"));
        contribuicao.setNome_adotado(rs.getString("nome_adotado"));
        return contribuicao;
    }

    private List<Contribuicao> buscar(String condicao, Object... valores){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT c.*, ma.mem_nome AS nome_anjo, mo.mem_nome AS nome_adotado FROM contribuicao c"
                        + " JOIN escoteiro ea ON ea.esc_cod = c.anjo_esc_cod"
                        + " JOIN membro ma ON ma.mem_cod = ea.mem_cod"
                        + " JOIN escoteiro eo ON eo.esc_cod = c.adotado_esc_cod"
                        + " JOIN membro mo ON mo.mem_cod = eo.mem_cod"
                        + " WHERE c.con_dt_fim IS NULL " + condicao
                        + " ORDER BY ma.mem_nome, mo.mem_nome", valores);
        List<Contribuicao> contribuicoes = new ArrayList<>();
        try {
            while (rs != null && rs.next())
                contribuicoes.add(preencher(rs));
        } catch (SQLException sqlex) {
            contribuicoes.clear();
        }
        return contribuicoes;
    }
}
