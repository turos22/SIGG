package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Responsavel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResponsavelDAO implements IDAO<Responsavel> {

    @Override
    public boolean gravar(Responsavel responsavel){
        Membro membro = responsavel.getMembro();
        if (membro.getMem_cod() == 0 && !new MembroDAO().gravar(membro))
            return false;
        Conexao conexao = SingletonDB.getConexao();
        int ultimo = conexao.getMaxPK("responsável", "resp_cod");
        if (ultimo < 0)
            return false;
        responsavel.setResp_cod(ultimo + 1);
        return conexao.manipular("INSERT INTO responsável (mem_cod, resp_cod) VALUES (?, ?)",
                membro.getMem_cod(), responsavel.getResp_cod());
    }

    @Override
    public boolean alterar(Responsavel responsavel){
        return new MembroDAO().alterar(responsavel.getMembro());
    }

    @Override
    public boolean apagar(Responsavel responsavel){
        return SingletonDB.getConexao().manipular("DELETE FROM responsável WHERE mem_cod = ?",
                responsavel.getMembro().getMem_cod());
    }
    @Override
    public List<Responsavel> get(){
        return buscar("", "");
    }
    @Override
    public List<Responsavel> get(String filtro){
        return buscar("", "AND m.mem_nome ILIKE ?", "%" + filtro + "%");
    }

    @Override
    public Responsavel get(int id){
        List<Responsavel> responsaveis = buscar("", "AND r.mem_cod = ?", id);
        return responsaveis.isEmpty() ? null : responsaveis.get(0);
    }


    public List<Responsavel> doEscoteiro(int memCodEscoteiro){
        return buscar(" JOIN escoteiro_responsavel er ON er.responsável_mem_cod = r.mem_cod",
                "AND er.escoteiro_mem_cod = ?", memCodEscoteiro);
    }

    public boolean existe(int memCod){
        ResultSet rs = SingletonDB.getConexao().consultar("SELECT 1 FROM responsável WHERE mem_cod = ?", memCod);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    private List<Responsavel> buscar(String juncao, String condicao, Object... valores){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT m.*, r.resp_cod FROM responsável r JOIN membro m ON m.mem_cod = r.mem_cod" + juncao
                        + " WHERE m.mem_dt_fim IS NULL " + condicao + " ORDER BY m.mem_nome", valores);
        List<Responsavel> responsaveis = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                Responsavel responsavel = new Responsavel();
                responsavel.setMembro(new MembroDAO().preencher(rs));
                responsavel.setResp_cod(rs.getInt("resp_cod"));
                responsaveis.add(responsavel);
            }
        } catch (SQLException sqlex) {
            responsaveis.clear();
        }
        return responsaveis;
    }
}
