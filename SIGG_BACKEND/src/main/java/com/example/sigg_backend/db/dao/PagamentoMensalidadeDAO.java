package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.FormaPagamento;
import com.example.sigg_backend.models.PagamentoMensalidade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PagamentoMensalidadeDAO {

    private static final int ATIVIDADE_MENSALIDADE = 1;

    public FormaPagamento[] listarFormasPagamento(Conexao conexao) {
        List<FormaPagamento> lista = new ArrayList<>();
        try (ResultSet rs = conexao.consultar(
                "SELECT tp_cod, tp_desc FROM tipo_pagamento ORDER BY tp_cod")) {
            while (rs != null && rs.next()) {
                lista.add(new FormaPagamento(rs.getInt("tp_cod"), rs.getString("tp_desc")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar formas de pagamento: " + e.getMessage(), e);
        }
        return lista.toArray(new FormaPagamento[0]);
    }

    public Escoteiro[] listarEscoteirosComResumo(Conexao conexao) {
        List<Escoteiro> lista = new ArrayList<>();
        String sql = "SELECT m.*, e.*, p.pat_nome FROM escoteiro e"
                + " JOIN membro m ON m.mem_cod = e.mem_cod"
                + " JOIN patrulha p ON p.pat_cod = e.patrulha_pat_cod2"
                + " WHERE m.mem_dt_fim IS NULL"
                + " ORDER BY e.esc_sit_fin DESC, m.mem_nome ASC";
        try (ResultSet rs = conexao.consultar(sql)) {
            EscoteiroDAO escDao = new EscoteiroDAO();
            while (rs != null && rs.next()) {
                Escoteiro esc = new Escoteiro();
                escDao.preencher(esc, rs);
                lista.add(esc);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar escoteiros: " + e.getMessage(), e);
        }
        return lista.toArray(new Escoteiro[0]);
    }

    public PagamentoMensalidade[] listarPorEscoteiro(Conexao conexao, int escCod) {
        return buscar(conexao, "AND me.escoteiro_esc_cod = ?", escCod);
    }

    public PagamentoMensalidade buscarPorConta(Conexao conexao, int crCod) {
        PagamentoMensalidade[] lista = buscar(conexao, "AND cr.cr_cod = ?", crCod);
        return lista.length == 0 ? null : lista[0];
    }

    public boolean registrar(Conexao conexao, PagamentoMensalidade pag) {
        LocalDate dtPagto = pag.getDataPagamento() != null ? pag.getDataPagamento() : LocalDate.now();
        String descParcela = limitar((pag.getFormaPagamentoCod() == 4 ? "Apadrinhamento " : "Mensalidade ") + pag.getReferencia());

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
            throw new IllegalStateException("Erro ao registrar pagamento: " + e.getMessage(), e);
        }

        if (!gravouBaixa) return false;

        BigDecimal diferenca = pag.verificarDivergencia(pag.getValorRecebido());
        if (diferenca.compareTo(BigDecimal.ZERO) < 0) {
            int ultimoPg = conexao.getMaxPK("pagamento", "pg_cod");
            if (ultimoPg >= 0) {
                LocalDate vencto = pag.getDataVencimento() != null ? pag.getDataVencimento() : dtPagto;
                conexao.manipular(
                        "INSERT INTO pagamento (pg_cod, pg_dt_pagto, pg_dt_vencto, pg_desc, tipo_pagamento_tp_cod,"
                                + " contas_receber_cr_cod, pg_valor_pago) VALUES (?, NULL, ?, ?, ?, ?, 0)",
                        ultimoPg + 1, vencto, limitar("Saldo Mensalidade " + pag.getReferencia()),
                        pag.getFormaPagamentoCod(), pag.getCrCod()
                );
            }
        }

        return true;
    }

    public boolean estornar(Conexao conexao, PagamentoMensalidade pag) {
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
                            limitar("Mensalidade " + pag.getReferencia()), primeiroPg
                    );
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao estornar pagamento: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean possuiMensalidadeVencidaEmAberto(Conexao conexao, int escCod) {
        try (ResultSet rs = conexao.consultar(
                "SELECT 1 FROM mensalidade me"
                        + " JOIN contas_receber cr ON cr.mensalidade_mens_cod = me.mens_cod"
                        + " AND cr.atividade_especial_atv_cod = " + ATIVIDADE_MENSALIDADE
                        + " LEFT JOIN pagamento p ON p.contas_receber_cr_cod = cr.cr_cod"
                        + " WHERE me.escoteiro_esc_cod = ?"
                        + " GROUP BY me.mens_cod, cr.cr_cod"
                        + " HAVING COALESCE(SUM(p.pg_valor_pago), 0) < cr.cr_valor"
                        + " AND MIN(p.pg_dt_vencto) < CURRENT_DATE",
                escCod)) {
            return rs != null && rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public void atualizarSituacaoFinanceiraEscoteiro(Conexao conexao, int escCod, int sitFin) {
        conexao.manipular(
                "UPDATE escoteiro SET esc_sit_fin = ? WHERE esc_cod = ?",
                sitFin, escCod
        );
    }

    private PagamentoMensalidade[] buscar(Conexao conexao, String condicao, Object... valores) {
        String sql = "SELECT cr.cr_cod, cr.cr_valor, me.mens_cod, me.mens_mes, me.mens_ano,"
                + " me.escoteiro_esc_cod, m.mem_nome, m.mem_cpf, e.esc_sit_fin,"
                + " MIN(p.pg_dt_vencto) AS vencimento,"
                + " MAX(p.pg_dt_pagto) AS ultimo_pagamento,"
                + " COALESCE(SUM(p.pg_valor_pago), 0) AS valor_pago,"
                + " MAX(CASE WHEN p.pg_dt_pagto IS NOT NULL THEN p.pg_cod ELSE 0 END) AS pg_cod,"
                + " MAX(CASE WHEN p.pg_dt_pagto IS NOT NULL THEN tp.tp_desc ELSE NULL END) AS forma_pagamento"
                + " FROM mensalidade me"
                + " JOIN contas_receber cr ON cr.mensalidade_mens_cod = me.mens_cod"
                + " AND cr.atividade_especial_atv_cod = " + ATIVIDADE_MENSALIDADE
                + " JOIN escoteiro e ON e.esc_cod = me.escoteiro_esc_cod"
                + " JOIN membro m ON m.mem_cod = e.mem_cod"
                + " LEFT JOIN pagamento p ON p.contas_receber_cr_cod = cr.cr_cod"
                + " LEFT JOIN tipo_pagamento tp ON tp.tp_cod = p.tipo_pagamento_tp_cod"
                + " WHERE 1 = 1 " + condicao
                + " GROUP BY cr.cr_cod, me.mens_cod, e.mem_cod, m.mem_cod"
                + " ORDER BY me.mens_ano DESC, me.mens_mes DESC, cr.cr_cod DESC";

        List<PagamentoMensalidade> lista = new ArrayList<>();
        LocalDate hoje = LocalDate.now();

        try (ResultSet rs = conexao.consultar(sql, valores)) {
            while (rs != null && rs.next()) {
                PagamentoMensalidade item = new PagamentoMensalidade();
                item.setId(rs.getInt("pg_cod"));
                item.setCrCod(rs.getInt("cr_cod"));
                item.setMensCod(rs.getInt("mens_cod"));
                item.setEscCod(rs.getInt("escoteiro_esc_cod"));
                item.setEscoteiroNome(rs.getString("mem_nome"));
                item.setEscoteiroCpf(rs.getString("mem_cpf"));
                item.setReferencia(String.format("%02d/%04d", rs.getInt("mens_mes"), rs.getInt("mens_ano")));
                item.setDataVencimento(Datas.ler(rs, "vencimento"));
                item.setDataPagamento(Datas.ler(rs, "ultimo_pagamento"));
                item.setValorDevido(BigDecimal.valueOf(rs.getDouble("cr_valor")).setScale(2, RoundingMode.HALF_UP));
                item.setValorPago(BigDecimal.valueOf(rs.getDouble("valor_pago")).setScale(2, RoundingMode.HALF_UP));
                item.setFormaPagamentoDesc(rs.getString("forma_pagamento"));
                item.setInadimplente(rs.getInt("esc_sit_fin") == 1);

                BigDecimal saldo = item.saldoRestante();
                if (saldo.compareTo(BigDecimal.ZERO) == 0) {
                    item.setSituacao("PAGA");
                } else if (item.getDataVencimento() != null && item.getDataVencimento().isBefore(hoje)) {
                    item.setSituacao("ATRASADA");
                } else if (item.getValorPago().compareTo(BigDecimal.ZERO) > 0) {
                    item.setSituacao("PARCIAL");
                } else {
                    item.setSituacao("ABERTA");
                }
                lista.add(item);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao consultar mensalidades: " + e.getMessage(), e);
        }
        return lista.toArray(new PagamentoMensalidade[0]);
    }

    private String limitar(String texto) {
        if (texto == null) return "Mensalidade";
        return texto.length() > 30 ? texto.substring(0, 30) : texto;
    }
}