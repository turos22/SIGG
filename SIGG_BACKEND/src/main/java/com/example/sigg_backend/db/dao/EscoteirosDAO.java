package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.Escoteiros;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EscoteirosDAO implements IDAO<Escoteiros> {
    @Override
    public boolean gravar(Escoteiros entidade){
        return SingletonDB.getConexao().manipular("INSERT INTO ESCOTEIROS(ID, NOME) VALUES(?,?)",
                entidade.getId(), entidade.getNome());
    }
    @Override
    public boolean alterar(Escoteiros entidade){
        return true;
    }
    @Override
    public boolean apagar(Escoteiros entidade){
        return true;
    }
    @Override
    public List<Escoteiros> get(){
        return buscar("");
    }
    @Override
    public List<Escoteiros> get(String filtro){
        return buscar("AND m.mem_nome ILIKE ?", "%" + filtro + "%");
    }
    @Override
    public Escoteiros get(int id){
        List<Escoteiros> escoteiros = buscar("AND e.esc_cod = ?", id);
        return escoteiros.isEmpty() ? null : escoteiros.get(0);
    }

    private List<Escoteiros> buscar(String condicao, Object... valores){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT e.esc_cod, m.mem_nome, m.mem_dt_nascto, p.pat_nome, e.esc_sit_fin"
                        + " FROM escoteiro e"
                        + " JOIN membro m ON m.mem_cod = e.mem_cod"
                        + " JOIN patrulha p ON p.pat_cod = e.patrulha_pat_cod2"
                        + " WHERE m.mem_dt_fim IS NULL " + condicao
                        + " ORDER BY m.mem_nome", valores);
        List<Escoteiros> escoteiros = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                Escoteiros escoteiro = new Escoteiros();
                escoteiro.setId(rs.getInt("esc_cod"));
                escoteiro.setNome(rs.getString("mem_nome"));
                escoteiro.setDataNascimento(rs.getDate("mem_dt_nascto").toLocalDate());
                escoteiro.setPatrulha(rs.getString("pat_nome"));
                escoteiro.setInadimplente(rs.getInt("esc_sit_fin") == 1);
                escoteiros.add(escoteiro);
            }
        } catch (SQLException sqlex) {
            escoteiros.clear();
        }
        return escoteiros;
    }
}
