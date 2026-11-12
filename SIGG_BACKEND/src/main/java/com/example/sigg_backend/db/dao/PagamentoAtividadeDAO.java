package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.PagamentoAtividade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PagamentoAtividadeDAO {

    private static final int ATIVIDADE_MENSALIDADE = 1;

    public PagamentoAtividade[] listarPorEscoteiro(int escCod) {
        return buscar("AND me.escoteiro_esc_cod = ?", escCod);
    }

    public PagamentoAtividade buscarPorConta(int crCod) {
        PagamentoAtividade[] lista = buscar("AND cr.cr_cod = ?", crCod);
        return lista.length == 0 ? null : lista[0];
    }

    public boolean registrar(PagamentoAtividade pag) {
        Conexao conexao = SingletonDB.getConexao();
        LocalDate dtPagto = pag.getDataPagamento() != null ? pag.getDataPagamento() : LocalDate.now();
        String descParcela = limitar("Ativ. " + pag.getAtividadeNome());

        boolean gravouBaixa;
        try (ResultSet rs = conexao.consultar(
                "SELECT pg_cod FROM pagamento WHERE contas_receber_cr_cod = ? AND pg_dt_pagto IS NULL ORDER BY pg_cod LIMIT 1",
                pag.getCrCod())) {
            if (rs != null && rs.next()) {
                int pgCodAberto = rs.getInt("pg_cod");
                pag.setId(pgCodAberto);
                gravouBaixa = conexao.manipular(
                        "UPDATE pagamento SET pg_dt_pagto = ?, pg_valor_pago = ?, tipo_pagamento_tp_cod = ?, pg_desc = ?"
                                + " WHERE pg_cod = ?",
                        dtPagto, pag.getValorRecebido(), pag.getFormaPagamentoCod(), descParcela, pgCodAberto
                );
            } else {
                int ultimoPg = conexao.getMaxPK("pagamento", "pg_cod");
                if (ultimoPg < 0) return false;
                pag.setId(ultimoPg + 1);
                LocalDate vencto = pag.getDataVencimento() != null ? pag.getDataVencimento() : dtPagto;
                gravouBaixa = conexao.manipular(
                        "INSERT INTO pagamento (pg_cod, pg_dt_pagto, pg_dt_vencto, pg_desc, tipo_pagamento_tp_cod,"
                                + " contas_receber_cr_cod, pg_valor_pago) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        pag.getId(), dtPagto, vencto, descParcela, pag.getFormaPagamentoCod(), pag.getCrCod(), pag.getValorRecebido()
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao registrar pagamento da atividade: " + e.getMessage(), e);
        }

        if (!gravouBaixa) return false;

        BigDecimal diferenca = pag.verificarDivergencia(pag.getValorRecebido());
        if (diferenca.compareTo(BigDecimal.ZERO) < 0 && pag.saldoRestante().compareTo(BigDecimal.ZERO) > 0) {
            int ultimoPg = conexao.getMaxPK("pagamento", "pg_cod");
            if (ultimoPg >= 0) {
                LocalDate vencto = pag.getDataVencimento() != null ? pag.getDataVencimento() : dtPagto;
                conexao.manipular(
                        "INSERT INTO pagamento (pg_cod, pg_dt_pagto, pg_dt_vencto, pg_desc, tipo_pagamento_tp_cod,"
                                + " contas_receber_cr_cod, pg_valor_pago) VALUES (?, NULL, ?, ?, ?, ?, 0)",
                        ultimoPg + 1, vencto, limitar("Saldo " + pag.getAtividadeNome()),
                        pag.getFormaPagamentoCod(), pag.getCrCod()
                );
            }
        }

        return true;
    }

    public boolean estornar(PagamentoAtividade pag) {
        Conexao conexao = SingletonDB.getConexao();
        try (ResultSet rs = conexao.consultar(
                "SELECT MIN(pg_cod) AS primeiro_pg FROM pagamento WHERE contas_receber_cr_cod = ?",
                pag.getCrCod())) {
            if (rs != null && rs.next()) {
                int primeiroPg = rs.getInt("primeiro_pg");
                if (primeiroPg > 0) {
                    conexao.manipular(
                            "DELETE FROM pagamento WHERE contas_receber_cr_cod = ? AND pg_cod <> ?",
                            pag.getCrCod(), primeiroPg
                    );
                    return conexao.manipular(
                            "UPDATE pagamento SET pg_dt_pagto = NULL, pg_valor_pago = 0, pg_desc = ? WHERE pg_cod = ?",
                            limitar(pag.getAtividadeNome()), primeiroPg
                    );
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao estornar pagamento da atividade: " + e.getMessage(), e);
        }
        return false;
    }

    private PagamentoAtividade[] buscar(String condicao, Object... valores) {
        String sql = "SELECT cr.cr_cod, cr.cr_valor, ae.atv_cod, ae.atv_nome, ae.atv_desc, ae.atv_data,"
                + " me.escoteiro_esc_cod, m.mem_nome, m.mem_cpf, e.esc_sit_fin,"
                + " MIN(p.pg_dt_vencto) AS vencimento,"
                + " MAX(p.pg_dt_pagto) AS ultimo_pagamento,"
                + " COALESCE(SUM(p.pg_valor_pago), 0) AS valor_pago,"
                + " MAX(CASE WHEN p.pg_dt_pagto IS NOT NULL THEN p.pg_cod ELSE 0 END) AS pg_cod,"
                + " MAX(CASE WHEN p.pg_dt_pagto IS NOT NULL THEN tp.tp_desc ELSE NULL END) AS forma_pagamento"
                + " FROM contas_receber cr"
                + " JOIN atividade_especial ae ON ae.atv_cod = cr.atividade_especial_atv_cod"
                + " AND cr.atividade_especial_atv_cod <> " + ATIVIDADE_MENSALIDADE
                + " JOIN mensalidade me ON me.mens_cod = cr.mensalidade_mens_cod"
                + " JOIN escoteiro e ON e.esc_cod = me.escoteiro_esc_cod"
                + " JOIN membro m ON m.mem_cod = e.mem_cod"
                + " LEFT JOIN pagamento p ON p.contas_receber_cr_cod = cr.cr_cod"
                + " LEFT JOIN tipo_pagamento tp ON tp.tp_cod = p.tipo_pagamento_tp_cod"
                + " WHERE 1 = 1 " + condicao
                + " GROUP BY cr.cr_cod, ae.atv_cod, me.mens_cod, e.mem_cod, m.mem_cod"
                + " ORDER BY ae.atv_data DESC, cr.cr_cod DESC";

        List<PagamentoAtividade> lista = new ArrayList<>();
        try (ResultSet rs = SingletonDB.getConexao().consultar(sql, valores)) {
            while (rs != null && rs.next()) {
                PagamentoAtividade item = new PagamentoAtividade();
                item.setId(rs.getInt("pg_cod"));
                item.setCrCod(rs.getInt("cr_cod"));
                item.setAtvCod(rs.getInt("atv_cod"));
                item.setAtividadeNome(rs.getString("atv_nome"));
                item.setAtividadeDesc(rs.getString("atv_desc"));
                item.setAtividadeData(Datas.ler(rs, "atv_data"));
                item.setEscCod(rs.getInt("escoteiro_esc_cod"));
                item.setEscoteiroNome(rs.getString("mem_nome"));
                item.setEscoteiroCpf(rs.getString("mem_cpf"));
                item.setDataVencimento(Datas.ler(rs, "vencimento"));
                item.setDataPagamento(Datas.ler(rs, "ultimo_pagamento"));
                item.setValorAtividade(BigDecimal.valueOf(rs.getDouble("cr_valor")).setScale(2, RoundingMode.HALF_UP));
                item.setValorPago(BigDecimal.valueOf(rs.getDouble("valor_pago")).setScale(2, RoundingMode.HALF_UP));
                item.setFormaPagamentoDesc(rs.getString("forma_pagamento"));
                item.setInadimplente(rs.getInt("esc_sit_fin") == 1);

                BigDecimal saldo = item.saldoRestante();
                if (saldo.compareTo(BigDecimal.ZERO) == 0) {
                    item.setSituacao("PAGA");
                } else if (item.getValorPago().compareTo(BigDecimal.ZERO) > 0) {
                    item.setSituacao("PARCIAL");
                } else {
                    item.setSituacao("ABERTA");
                }
                lista.add(item);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao consultar atividades do escoteiro: " + e.getMessage(), e);
        }
        return lista.toArray(new PagamentoAtividade[0]);
    }

    private String limitar(String texto) {
        if (texto == null) return "Atividade";
        return texto.length() > 30 ? texto.substring(0, 30) : texto;
    }
}