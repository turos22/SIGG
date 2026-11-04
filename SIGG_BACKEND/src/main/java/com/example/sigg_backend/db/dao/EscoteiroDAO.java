package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Patrulha;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EscoteiroDAO implements IDAO<Escoteiro> {
    @Override
    public boolean gravar(Escoteiro escoteiro){
        Membro membro = escoteiro.getMembro();
        if (membro.getMem_cod() == 0 && !new MembroDAO().gravar(membro))
            return false;
        Conexao conexao = SingletonDB.getConexao();
        int ultimo = conexao.getMaxPK("escoteiro", "esc_cod");
        if (ultimo < 0)
            return false;
        escoteiro.setEsc_cod(ultimo + 1);
        if (escoteiro.getAnjo() == 0)
            escoteiro.setAnjo(escoteiro.getEsc_cod()); // convenção do dump: sem Anjo aponta para si mesmo
        return conexao.manipular("INSERT INTO escoteiro (mem_cod, esc_cod, categoria_mensalidade_cat_mens_cod,"
                        + " escoteiro_esc_cod, patrulha_pat_cod2, esc_sit_fin) VALUES (?, ?, ?, ?, ?, ?)",
                membro.getMem_cod(), escoteiro.getEsc_cod(), escoteiro.getCat_mensalidade(), escoteiro.getAnjo(),
                escoteiro.getPatrulha().getCod(), escoteiro.getEsc_sit_fin());
    }
    @Override
    public boolean alterar(Escoteiro escoteiro){
        return new MembroDAO().alterar(escoteiro.getMembro()) && SingletonDB.getConexao().manipular(
                "UPDATE escoteiro SET categoria_mensalidade_cat_mens_cod = ?, escoteiro_esc_cod = ?, patrulha_pat_cod2 = ?,"
                        + " esc_sit_fin = ? WHERE mem_cod = ?",
                escoteiro.getCat_mensalidade(), escoteiro.getAnjo(), escoteiro.getPatrulha().getCod(),
                escoteiro.getEsc_sit_fin(), escoteiro.getMembro().getMem_cod());
    }

    @Override
    public boolean apagar(Escoteiro escoteiro){
        return SingletonDB.getConexao().manipular("DELETE FROM escoteiro WHERE mem_cod = ?",
                escoteiro.getMembro().getMem_cod());
    }
    @Override
    public List<Escoteiro> get(){
        return buscar("");
    }
    @Override
    public List<Escoteiro> get(String filtro){
        return buscar("AND m.mem_nome ILIKE ?", "%" + filtro + "%");
    }

    @Override
    public Escoteiro get(int id){
        List<Escoteiro> escoteiros = buscar("AND e.mem_cod = ?", id);
        if (escoteiros.isEmpty())
            return null;
        Escoteiro escoteiro = escoteiros.get(0);
        escoteiro.setResponsaveis(new ResponsavelDAO().doEscoteiro(id));
        return escoteiro;
    }

    public boolean existe(int memCod){
        ResultSet rs = SingletonDB.getConexao().consultar("SELECT 1 FROM escoteiro WHERE mem_cod = ?", memCod);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    public void preencher(Escoteiro escoteiro, ResultSet rs) throws SQLException {
        escoteiro.setMembro(new MembroDAO().preencher(rs));
        escoteiro.setEsc_cod(rs.getInt("esc_cod"));
        escoteiro.setCat_mensalidade(rs.getInt("categoria_mensalidade_cat_mens_cod"));
        escoteiro.setAnjo(rs.getInt("escoteiro_esc_cod"));
        escoteiro.setEsc_sit_fin(rs.getInt("esc_sit_fin"));
        Patrulha patrulha = new Patrulha();
        patrulha.setCod(rs.getInt("patrulha_pat_cod2"));
        patrulha.setNome(rs.getString("pat_nome"));
        escoteiro.setPatrulha(patrulha);
    }

    private List<Escoteiro> buscar(String condicao, Object... valores){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT m.*, e.*, p.pat_nome FROM escoteiro e"
                        + " JOIN membro m ON m.mem_cod = e.mem_cod"
                        + " JOIN patrulha p ON p.pat_cod = e.patrulha_pat_cod2"
                        + " WHERE m.mem_dt_fim IS NULL " + condicao
                        + " ORDER BY m.mem_nome", valores);
        List<Escoteiro> escoteiros = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                Escoteiro escoteiro = new Escoteiro();
                preencher(escoteiro, rs);
                escoteiros.add(escoteiro);
            }
        } catch (SQLException sqlex) {
            escoteiros.clear();
        }
        return escoteiros;
    }
}
