package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Responsavel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResponsavelDAO implements IDAO<Responsavel> {

    @Override
    public boolean gravar(Conexao conexao, Responsavel responsavel){
        Membro membro = responsavel.getMembro();
        if (membro.getMem_cod() == 0 && !new MembroDAO().gravar(conexao, membro))
            return false;
        int ultimo = conexao.getMaxPK("responsável", "resp_cod");
        if (ultimo < 0)
            return false;
        responsavel.setResp_cod(ultimo + 1);
        return conexao.manipular("INSERT INTO responsável (mem_cod, resp_cod) VALUES (?, ?)",
                membro.getMem_cod(), responsavel.getResp_cod());
    }

    @Override
    public boolean alterar(Conexao conexao, Responsavel responsavel){
        return new MembroDAO().alterar(conexao, responsavel.getMembro());
    }

    @Override
    public boolean apagar(Conexao conexao, Responsavel responsavel){
        return conexao.manipular("DELETE FROM responsável WHERE mem_cod = ?",
                responsavel.getMembro().getMem_cod());
    }
    @Override
    public List<Responsavel> get(Conexao conexao){
        return buscar(conexao, "", "");
    }
    @Override
    public List<Responsavel> get(Conexao conexao, String filtro){
        return buscar(conexao, "", "AND m.mem_nome ILIKE ?", "%" + filtro + "%");
    }

    @Override
    public Responsavel get(Conexao conexao, int id){
        List<Responsavel> responsaveis = buscar(conexao, "", "AND r.mem_cod = ?", id);
        return responsaveis.isEmpty() ? null : responsaveis.get(0);
    }


    public List<Responsavel> doEscoteiro(Conexao conexao, int memCodEscoteiro){
        return buscar(conexao, " JOIN escoteiro_responsavel er ON er.responsável_mem_cod = r.mem_cod",
                "AND er.escoteiro_mem_cod = ?", memCodEscoteiro);
    }

    public boolean vincular(Conexao conexao, int memCodResp, int memCodEscoteiro){
        return conexao.manipular("INSERT INTO escoteiro_responsavel (responsável_mem_cod, escoteiro_mem_cod) VALUES (?, ?)",
                memCodResp, memCodEscoteiro);
    }

    public boolean desvincular(Conexao conexao, int memCodResp, int memCodEscoteiro){
        return conexao.manipular("DELETE FROM escoteiro_responsavel WHERE responsável_mem_cod = ? AND escoteiro_mem_cod = ?",
                memCodResp, memCodEscoteiro);
    }

    public boolean existeVinculo(Conexao conexao, int memCodResp, int memCodEscoteiro){
        ResultSet rs = conexao.consultar("SELECT 1 FROM escoteiro_responsavel"
                + " WHERE responsável_mem_cod = ? AND escoteiro_mem_cod = ?", memCodResp, memCodEscoteiro);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    public int contarVinculos(Conexao conexao, int memCodResp){
        ResultSet rs = conexao.consultar("SELECT count(*) FROM escoteiro_responsavel WHERE responsável_mem_cod = ?", memCodResp);
        try {
            return rs != null && rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException sqlex) {
            return 0;
        }
    }

    public boolean existe(Conexao conexao, int memCod){
        ResultSet rs = conexao.consultar("SELECT 1 FROM responsável WHERE mem_cod = ?", memCod);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    private List<Responsavel> buscar(Conexao conexao, String juncao, String condicao, Object... valores){
        ResultSet rs = conexao.consultar(
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
