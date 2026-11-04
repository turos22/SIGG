package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.Financeiro;
import com.example.sigg_backend.models.Membro;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FinanceiroDAO implements IDAO<Financeiro> {

    @Override
    public boolean gravar(Financeiro financeiro){
        Membro membro = financeiro.getMembro();
        if (membro.getMem_cod() == 0 && !new MembroDAO().gravar(membro))
            return false;
        Conexao conexao = SingletonDB.getConexao();
        int ultimo = conexao.getMaxPK("financeiro", "fin_cod");
        if (ultimo < 0)
            return false;
        financeiro.setFin_cod(ultimo + 1);
        return conexao.manipular("INSERT INTO financeiro (mem_cod, fin_cod) VALUES (?, ?)",
                membro.getMem_cod(), financeiro.getFin_cod());
    }

    @Override
    public boolean alterar(Financeiro financeiro){
        return new MembroDAO().alterar(financeiro.getMembro());
    }

    @Override
    public boolean apagar(Financeiro financeiro){
        return SingletonDB.getConexao().manipular("DELETE FROM financeiro WHERE mem_cod = ?",
                financeiro.getMembro().getMem_cod());
    }
    @Override
    public List<Financeiro> get(){
        return buscar("");
    }
    @Override
    public List<Financeiro> get(String filtro){
        return buscar("AND m.mem_nome ILIKE ?", "%" + filtro + "%");
    }

    @Override
    public Financeiro get(int id){
        List<Financeiro> financeiros = buscar("AND f.mem_cod = ?", id);
        return financeiros.isEmpty() ? null : financeiros.get(0);
    }

    public boolean existe(int memCod){
        ResultSet rs = SingletonDB.getConexao().consultar("SELECT 1 FROM financeiro WHERE mem_cod = ?", memCod);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    private List<Financeiro> buscar(String condicao, Object... valores){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT m.*, f.fin_cod FROM financeiro f JOIN membro m ON m.mem_cod = f.mem_cod"
                        + " WHERE m.mem_dt_fim IS NULL " + condicao + " ORDER BY m.mem_nome", valores);
        List<Financeiro> financeiros = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                Financeiro financeiro = new Financeiro();
                financeiro.setMembro(new MembroDAO().preencher(rs));
                financeiro.setFin_cod(rs.getInt("fin_cod"));
                financeiros.add(financeiro);
            }
        } catch (SQLException sqlex) {
            financeiros.clear();
        }
        return financeiros;
    }
}
