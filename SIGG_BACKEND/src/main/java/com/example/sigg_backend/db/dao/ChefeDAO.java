package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.models.Chefe;
import com.example.sigg_backend.models.Tropa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChefeDAO implements IDAO<Chefe> {

    @Override
    public boolean gravar(Conexao conexao, Chefe chefe){
        EscoteiroDAO escoteiroDAO = new EscoteiroDAO();
        int memCod = chefe.getMembro().getMem_cod();
        if ((memCod == 0 || !escoteiroDAO.existe(conexao, memCod)) && !escoteiroDAO.gravar(conexao, chefe))
            return false;
        int ultimo = conexao.getMaxPK("chefe", "chefe_cod");
        if (ultimo < 0)
            return false;
        chefe.setChefe_cod(ultimo + 1);
        return conexao.manipular("INSERT INTO chefe (mem_cod, chefe_cod, chefe_gestao_ini, chefe_gestao_fim, tropa_tro_cod)"
                        + " VALUES (?, ?, ?, ?, ?)",
                chefe.getMembro().getMem_cod(), chefe.getChefe_cod(), chefe.getGestao_ini(), chefe.getGestao_fim(),
                chefe.getTropa().getCod());
    }
    @Override
    public boolean alterar(Conexao conexao, Chefe chefe){
        return new EscoteiroDAO().alterar(conexao, chefe) && conexao.manipular(
                "UPDATE chefe SET chefe_gestao_ini = ?, chefe_gestao_fim = ?, tropa_tro_cod = ? WHERE mem_cod = ?",
                chefe.getGestao_ini(), chefe.getGestao_fim(), chefe.getTropa().getCod(), chefe.getMembro().getMem_cod());
    }

    @Override
    public boolean apagar(Conexao conexao, Chefe chefe){
        return conexao.manipular("DELETE FROM chefe WHERE mem_cod = ?", chefe.getMembro().getMem_cod());
    }
    @Override
    public List<Chefe> get(Conexao conexao){
        return buscar(conexao, "");
    }
    @Override
    public List<Chefe> get(Conexao conexao, String filtro){
        return buscar(conexao, "AND m.mem_nome ILIKE ?", "%" + filtro + "%");
    }

    @Override
    public Chefe get(Conexao conexao, int id){
        List<Chefe> chefes = buscar(conexao, "AND c.mem_cod = ?", id);
        return chefes.isEmpty() ? null : chefes.get(0);
    }

    public boolean existe(Conexao conexao, int memCod){
        ResultSet rs = conexao.consultar("SELECT 1 FROM chefe WHERE mem_cod = ?", memCod);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    private List<Chefe> buscar(Conexao conexao, String condicao, Object... valores){
        ResultSet rs = conexao.consultar(
                "SELECT " + EscoteiroDAO.COLUNAS + ", c.chefe_cod, c.chefe_gestao_ini, c.chefe_gestao_fim,"
                        + " c.tropa_tro_cod AS chefe_tro_cod, t.tro_nome AS chefe_tro_nome"
                        + " FROM chefe c"
                        + " JOIN escoteiro e ON e.mem_cod = c.mem_cod"
                        + " JOIN membro m ON m.mem_cod = c.mem_cod"
                        + EscoteiroDAO.JUNCAO_PATRULHA
                        + " JOIN tropa t ON t.tro_cod = c.tropa_tro_cod"
                        + " WHERE m.mem_dt_fim IS NULL " + condicao
                        + " ORDER BY m.mem_nome", valores);
        List<Chefe> chefes = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                Chefe chefe = new Chefe();
                new EscoteiroDAO().preencher(chefe, rs);
                chefe.setChefe_cod(rs.getInt("chefe_cod"));
                chefe.setGestao_ini(Datas.ler(rs, "chefe_gestao_ini"));
                chefe.setGestao_fim(Datas.ler(rs, "chefe_gestao_fim"));
                Tropa tropa = new Tropa();
                tropa.setCod(rs.getInt("chefe_tro_cod"));
                tropa.setNome(rs.getString("chefe_tro_nome"));
                chefe.setTropa(tropa);
                chefes.add(chefe);
            }
        } catch (SQLException sqlex) {
            chefes.clear();
        }
        return chefes;
    }
}
