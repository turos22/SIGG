package com.example.sigg_backend.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Patrulha;
import com.example.sigg_backend.models.Tropa;

public class PatrulhaDAO implements IDAO<Patrulha> {

    @Override
    public boolean gravar(Conexao conexao, Patrulha patrulha){
        int ultimo = conexao.getMaxPK("patrulha", "pat_cod");
        if (ultimo < 0)
            return false;
        patrulha.setCod(ultimo + 1);
        return conexao.manipular("INSERT INTO patrulha (pat_cod, pat_nome, pat_lider_cod, pat_sublider_cod, tropa_tro_cod)"
                        + " VALUES (?, ?, ?, ?, ?)",
                patrulha.getCod(), patrulha.getNome(), codigoDe(patrulha.getLider()), codigoDe(patrulha.getSublider()),
                patrulha.getTropa().getCod());
    }

    @Override
    public boolean alterar(Conexao conexao, Patrulha patrulha){
        return conexao.manipular("UPDATE patrulha SET pat_nome = ?, pat_lider_cod = ?, pat_sublider_cod = ?,"
                        + " tropa_tro_cod = ? WHERE pat_cod = ?",
                patrulha.getNome(), codigoDe(patrulha.getLider()), codigoDe(patrulha.getSublider()),
                patrulha.getTropa().getCod(), patrulha.getCod());
    }

    @Override
    public boolean apagar(Conexao conexao, Patrulha patrulha){
        return conexao.manipular("DELETE FROM patrulha WHERE pat_cod = ?", patrulha.getCod());
    }

    @Override
    public List<Patrulha> get(Conexao conexao){
        return buscar(conexao, "");
    }

    @Override
    public List<Patrulha> get(Conexao conexao, String filtro){
        return buscar(conexao, "AND p.pat_nome ILIKE ?", "%" + filtro + "%");
    }

    @Override
    public Patrulha get(Conexao conexao, int id){
        List<Patrulha> patrulhas = buscar(conexao, "AND p.pat_cod = ?", id);
        return patrulhas.isEmpty() ? null : patrulhas.get(0);
    }

    public List<Patrulha> listar(Conexao conexao, Integer troCod){
        return troCod == null ? get(conexao) : buscar(conexao, "AND p.tropa_tro_cod = ?", troCod);
    }

    // Líder e sublíder precisam ser escoteiros ativos de alguma patrulha da tropa
    public boolean escoteiroNaTropa(Conexao conexao, int escCod, int troCod){
        return existe(conexao, "SELECT 1 FROM escoteiro e JOIN membro m ON m.mem_cod = e.mem_cod"
                + " JOIN patrulha p ON p.pat_cod = e.patrulha_pat_cod2"
                + " WHERE e.esc_cod = ? AND p.tropa_tro_cod = ? AND m.mem_dt_fim IS NULL", escCod, troCod);
    }

    // Qualquer escoteiro, até o desligado, impede a exclusão (a FK escoteiro_patrulha_fkv2 mantém o histórico)
    public boolean temEscoteiros(Conexao conexao, int patCod){
        return existe(conexao, "SELECT 1 FROM escoteiro WHERE patrulha_pat_cod2 = ?", patCod);
    }

    private boolean existe(Conexao conexao, String sql, Object... valores){
        ResultSet rs = conexao.consultar(sql, valores);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    private Integer codigoDe(Escoteiro escoteiro){
        return escoteiro == null ? null : escoteiro.getEsc_cod();
    }

    private List<Patrulha> buscar(Conexao conexao, String condicao, Object... valores){
        ResultSet rs = conexao.consultar(
                "SELECT p.pat_cod, p.pat_nome, t.tro_cod, t.tro_nome,"
                        + " el.esc_cod AS lider_esc, ml.mem_cod AS lider_mem, ml.mem_nome AS lider_nome,"
                        + " es.esc_cod AS sub_esc, ms.mem_cod AS sub_mem, ms.mem_nome AS sub_nome"
                        + " FROM patrulha p JOIN tropa t ON t.tro_cod = p.tropa_tro_cod"
                        + " LEFT JOIN escoteiro el ON el.esc_cod = p.pat_lider_cod"
                        + " LEFT JOIN membro ml ON ml.mem_cod = el.mem_cod"
                        + " LEFT JOIN escoteiro es ON es.esc_cod = p.pat_sublider_cod"
                        + " LEFT JOIN membro ms ON ms.mem_cod = es.mem_cod"
                        + " WHERE 1 = 1 " + condicao + " ORDER BY p.pat_cod", valores);
        List<Patrulha> patrulhas = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                Patrulha patrulha = new Patrulha();
                patrulha.setCod(rs.getInt("pat_cod"));
                patrulha.setNome(rs.getString("pat_nome"));
                patrulha.setTropa(new Tropa(rs.getInt("tro_cod"), rs.getString("tro_nome")));
                patrulha.setLider(resumo(rs, "lider_esc", "lider_mem", "lider_nome"));
                patrulha.setSublider(resumo(rs, "sub_esc", "sub_mem", "sub_nome"));
                patrulhas.add(patrulha);
            }
        } catch (SQLException sqlex) {
            patrulhas.clear();
        }
        return patrulhas;
    }

    // Escoteiro resumido: só o código e o nome, para não devolver CPF e endereço numa lista de patrulhas
    private Escoteiro resumo(ResultSet rs, String esc, String mem, String nome) throws SQLException {
        int escCod = rs.getInt(esc);
        if (rs.wasNull())
            return null;
        Membro membro = new Membro();
        membro.setMem_cod(rs.getInt(mem));
        membro.setNome(rs.getString(nome));
        Escoteiro escoteiro = new Escoteiro();
        escoteiro.setEsc_cod(escCod);
        escoteiro.setMembro(membro);
        return escoteiro;
    }
}
