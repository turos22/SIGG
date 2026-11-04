package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.Presidente;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PresidenteDAO implements IDAO<Presidente> {

    @Override
    public boolean gravar(Presidente presidente){
        EscoteiroDAO escoteiroDAO = new EscoteiroDAO();
        int memCod = presidente.getMembro().getMem_cod();
        if ((memCod == 0 || !escoteiroDAO.existe(memCod)) && !escoteiroDAO.gravar(presidente))
            return false;
        Conexao conexao = SingletonDB.getConexao();
        int ultimo = conexao.getMaxPK("presidente", "pres_cod");
        if (ultimo < 0)
            return false;
        presidente.setPres_cod(ultimo + 1);
        return conexao.manipular("INSERT INTO presidente (mem_cod, pres_cod, pres_gestao_ini, pres_gestao_fim) VALUES (?, ?, ?, ?)",
                presidente.getMembro().getMem_cod(), presidente.getPres_cod(), presidente.getGestao_ini(),
                presidente.getGestao_fim());
    }
    @Override
    public boolean alterar(Presidente presidente){
        return new EscoteiroDAO().alterar(presidente) && SingletonDB.getConexao().manipular(
                "UPDATE presidente SET pres_gestao_ini = ?, pres_gestao_fim = ? WHERE mem_cod = ?",
                presidente.getGestao_ini(), presidente.getGestao_fim(), presidente.getMembro().getMem_cod());
    }

    @Override
    public boolean apagar(Presidente presidente){
        return SingletonDB.getConexao().manipular("DELETE FROM presidente WHERE mem_cod = ?",
                presidente.getMembro().getMem_cod());
    }
    @Override
    public List<Presidente> get(){
        return buscar("");
    }
    @Override
    public List<Presidente> get(String filtro){
        return buscar("AND m.mem_nome ILIKE ?", "%" + filtro + "%");
    }

    @Override
    public Presidente get(int id){
        List<Presidente> presidentes = buscar("AND pr.mem_cod = ?", id);
        return presidentes.isEmpty() ? null : presidentes.get(0);
    }

    public boolean existe(int memCod){
        ResultSet rs = SingletonDB.getConexao().consultar("SELECT 1 FROM presidente WHERE mem_cod = ?", memCod);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }


    private List<Presidente> buscar(String condicao, Object... valores){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT m.*, e.*, p.pat_nome, pr.pres_cod, pr.pres_gestao_ini, pr.pres_gestao_fim"
                        + " FROM presidente pr"
                        + " JOIN escoteiro e ON e.mem_cod = pr.mem_cod"
                        + " JOIN membro m ON m.mem_cod = pr.mem_cod"
                        + " JOIN patrulha p ON p.pat_cod = e.patrulha_pat_cod2"
                        + " WHERE m.mem_dt_fim IS NULL " + condicao
                        + " ORDER BY m.mem_nome", valores);
        List<Presidente> presidentes = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                Presidente presidente = new Presidente();
                new EscoteiroDAO().preencher(presidente, rs);
                presidente.setPres_cod(rs.getInt("pres_cod"));
                presidente.setGestao_ini(Datas.ler(rs, "pres_gestao_ini"));
                presidente.setGestao_fim(Datas.ler(rs, "pres_gestao_fim"));
                presidentes.add(presidente);
            }
        } catch (SQLException sqlex) {
            presidentes.clear();
        }
        return presidentes;
    }
}
