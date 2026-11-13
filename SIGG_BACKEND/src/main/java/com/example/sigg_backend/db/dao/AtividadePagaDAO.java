package com.example.sigg_backend.db.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.AtividadePaga;

// Atividades pagas ficam em atividade_especial: as que têm atv_valor preenchido.
// (Reuniões semanais têm valor NULL e atv_cod 1 é o registro técnico das mensalidades.)
public class AtividadePagaDAO {

    private static final String COLUNAS = "SELECT atv_cod, atv_nome, atv_desc, atv_data, atv_local, atv_valor,"
            + " atv_vagas, atv_prazo_interesse FROM atividade_especial WHERE atv_valor IS NOT NULL";

    public boolean gravar(AtividadePaga atividade) {
        Conexao conexao = SingletonDB.getConexao();
        // O dump não gera chaves sozinho. MAX+1 pode repetir com dois cadastros simultâneos.
        int ultimo = conexao.getMaxPK("atividade_especial", "atv_cod");
        if (ultimo < 0)
            return false;
        atividade.setCodigo(ultimo + 1);
        return conexao.manipular(
                "INSERT INTO atividade_especial (atv_cod, atv_nome, atv_desc, atv_data, atv_local, atv_valor,"
                        + " atv_vagas, atv_prazo_interesse) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                atividade.getCodigo(), atividade.getNome(), atividade.getDescricao(), atividade.getData(),
                atividade.getLocal(), atividade.getValor(), atividade.getVagas(), atividade.getPrazoInteresse());
    }

    // somenteAbertas: prazo de manifestação ainda não venceu (as antigas, sem prazo, valem pela data da atividade).
    public List<AtividadePaga> listar(boolean somenteAbertas) {
        String sql = COLUNAS;
        if (somenteAbertas)
            sql += " AND COALESCE(atv_prazo_interesse, atv_data) >= CURRENT_DATE";
        return montarLista(SingletonDB.getConexao().consultar(sql + " ORDER BY atv_data DESC, atv_nome"));
    }

    public AtividadePaga get(int codigo) {
        List<AtividadePaga> lista = montarLista(SingletonDB.getConexao().consultar(COLUNAS + " AND atv_cod = ?", codigo));
        return lista.isEmpty() ? null : lista.get(0);
    }

    public boolean existeMesmoNomeEData(String nome, LocalDate data) {
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT 1 FROM atividade_especial WHERE LOWER(atv_nome) = LOWER(?) AND atv_data = ?", nome, data);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    private List<AtividadePaga> montarLista(ResultSet rs) {
        List<AtividadePaga> lista = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                AtividadePaga a = new AtividadePaga();
                a.setCodigo(rs.getInt("atv_cod"));
                a.setNome(rs.getString("atv_nome"));
                a.setDescricao(rs.getString("atv_desc"));
                a.setData(Datas.ler(rs, "atv_data"));
                a.setLocal(rs.getString("atv_local"));
                a.setValor(BigDecimal.valueOf(rs.getDouble("atv_valor")).setScale(2, RoundingMode.HALF_UP));
                int vagas = rs.getInt("atv_vagas");
                a.setVagas(rs.wasNull() ? null : vagas);
                a.setPrazoInteresse(Datas.ler(rs, "atv_prazo_interesse"));
                lista.add(a);
            }
        } catch (SQLException sqlex) {
            lista.clear();
        }
        return lista;
    }
}
