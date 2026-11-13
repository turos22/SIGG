package com.example.sigg_backend.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.models.Frequencia;
import com.example.sigg_backend.models.ItemChamada;
import com.example.sigg_backend.models.ResumoChamada;
import com.example.sigg_backend.models.Tropa;

public class FrequenciaDAO {

    // Escoteiro ativo da tropa = escoteiro de patrulha dela, de pessoa sem data de saída
    private static final String ESCOTEIROS_DA_TROPA = " FROM escoteiro e JOIN membro m ON m.mem_cod = e.mem_cod"
            + " JOIN patrulha p ON p.pat_cod = e.patrulha_pat_cod2"
            + " WHERE p.tropa_tro_cod = ? AND m.mem_dt_fim IS NULL";

    // Uma linha por escoteiro atual da tropa. data = null devolve o status vazio (só o percentual do ano).
    // Percentual = presenças (P) sobre todos os registros do escoteiro no ano civil; sem registro, 0.
    public List<ItemChamada> itensDaTropa(Conexao conexao, int troCod, LocalDate data, int ano){
        ResultSet rs = conexao.consultar(
                "SELECT e.esc_cod, m.mem_cod, m.mem_nome, p.pat_nome,"
                        + " (SELECT f.freq_status FROM frequencia f WHERE f.escoteiro_esc_cod = e.esc_cod"
                        + "   AND f.freq_data = CAST(? AS date)) AS status,"
                        + " (SELECT COALESCE(ROUND(100.0 * COUNT(*) FILTER (WHERE fa.freq_status = 'P')"
                        + "   / NULLIF(COUNT(*), 0), 1), 0) FROM frequencia fa WHERE fa.escoteiro_esc_cod = e.esc_cod"
                        + "   AND EXTRACT(YEAR FROM fa.freq_data) = ?) AS percentual"
                        + ESCOTEIROS_DA_TROPA + " ORDER BY m.mem_nome", data, ano, troCod);
        List<ItemChamada> itens = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                ItemChamada item = new ItemChamada();
                item.setEsc_cod(rs.getInt("esc_cod"));
                item.setMem_cod(rs.getInt("mem_cod"));
                item.setNome(rs.getString("mem_nome"));
                item.setPatrulha(rs.getString("pat_nome"));
                item.setStatus(rs.getString("status"));
                item.setPercentual_ano(rs.getBigDecimal("percentual"));
                itens.add(item);
            }
        } catch (SQLException sqlex) {
            itens.clear();
        }
        return itens;
    }

    public Set<Integer> escoteirosDaTropa(Conexao conexao, int troCod){
        ResultSet rs = conexao.consultar("SELECT e.esc_cod" + ESCOTEIROS_DA_TROPA, troCod);
        Set<Integer> codigos = new HashSet<>();
        try {
            while (rs != null && rs.next())
                codigos.add(rs.getInt("esc_cod"));
        } catch (SQLException sqlex) {
            codigos.clear();
        }
        return codigos;
    }

    // A tropa de uma frequência é a de hoje do escoteiro: o registro acompanha quem foi marcado
    public List<ResumoChamada> listarResumos(Conexao conexao, Integer troCod, int ano){
        ResultSet rs = conexao.consultar(
                "SELECT t.tro_cod, t.tro_nome, f.freq_data, COUNT(*) FILTER (WHERE f.freq_status = 'P') AS presentes,"
                        + " COUNT(*) AS total"
                        + " FROM frequencia f JOIN escoteiro e ON e.esc_cod = f.escoteiro_esc_cod"
                        + " JOIN patrulha p ON p.pat_cod = e.patrulha_pat_cod2"
                        + " JOIN tropa t ON t.tro_cod = p.tropa_tro_cod"
                        + " WHERE EXTRACT(YEAR FROM f.freq_data) = ?" + (troCod == null ? "" : " AND t.tro_cod = ?")
                        + " GROUP BY t.tro_cod, t.tro_nome, f.freq_data ORDER BY f.freq_data DESC, t.tro_nome",
                troCod == null ? new Object[] {ano} : new Object[] {ano, troCod});
        List<ResumoChamada> resumos = new ArrayList<>();
        try {
            while (rs != null && rs.next())
                resumos.add(new ResumoChamada(new Tropa(rs.getInt("tro_cod"), rs.getString("tro_nome")),
                        Datas.ler(rs, "freq_data"), rs.getInt("presentes"), rs.getInt("total")));
        } catch (SQLException sqlex) {
            resumos.clear();
        }
        return resumos;
    }

    public boolean existeRegistro(Conexao conexao, int troCod, LocalDate data){
        ResultSet rs = conexao.consultar("SELECT 1 FROM frequencia f WHERE f.freq_data = ? AND f.escoteiro_esc_cod IN"
                + " (SELECT e.esc_cod" + ESCOTEIROS_DA_TROPA + ")", data, troCod);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    public boolean gravar(Conexao conexao, Frequencia frequencia){
        int ultimo = conexao.getMaxPK("frequencia", "freq_cod");
        if (ultimo < 0)
            return false;
        frequencia.setFreq_cod(ultimo + 1);
        return conexao.manipular("INSERT INTO frequencia (freq_cod, freq_data, freq_status, escoteiro_esc_cod,"
                        + " atividade_especial_atv_cod) VALUES (?, ?, ?, ?, NULL)"
                        + " ON CONFLICT (escoteiro_esc_cod, freq_data) DO UPDATE SET freq_status = EXCLUDED.freq_status",
                frequencia.getFreq_cod(), frequencia.getData(), frequencia.getStatus(), frequencia.getEsc_cod());
    }

    public boolean apagarDaTropa(Conexao conexao, int troCod, LocalDate data){
        return conexao.manipular("DELETE FROM frequencia WHERE freq_data = ? AND escoteiro_esc_cod IN"
                + " (SELECT e.esc_cod" + ESCOTEIROS_DA_TROPA + ")", data, troCod);
    }
}
