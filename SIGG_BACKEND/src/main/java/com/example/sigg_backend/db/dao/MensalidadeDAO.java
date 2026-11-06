package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.Mensalidade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MensalidadeDAO implements IDAO<Mensalidade> {
    private static final int ATIVIDADE_MENSALIDADE = 1;
    private static final int TIPO_PAGAMENTO_PADRAO = 1;
    private static final int DIA_VENCIMENTO = 10;

    @Override
    public boolean gravar(Mensalidade mensalidade){
        Conexao conexao = SingletonDB.getConexao();
        int ultimaMensalidade = conexao.getMaxPK("mensalidade", "mens_cod");
        int ultimaConta = conexao.getMaxPK("contas_receber", "cr_cod");
        int ultimoPagamento = conexao.getMaxPK("pagamento", "pg_cod");
        if (ultimaMensalidade < 0 || ultimaConta < 0 || ultimoPagamento < 0)
            return false;
        mensalidade.setMens_cod(ultimaMensalidade + 1);
        mensalidade.setCr_cod(ultimaConta + 1);
        if (mensalidade.getMens_dt_lancto() == null)
            mensalidade.setMens_dt_lancto(LocalDate.now());
        if (mensalidade.getDt_vencimento() == null)
            mensalidade.setDt_vencimento(LocalDate.of(mensalidade.getMens_ano(), mensalidade.getMens_mes(), DIA_VENCIMENTO));
        String descricao = "Mensalidade " + mensalidade.referencia();
        return conexao.manipular("INSERT INTO mensalidade (mens_cod, mens_mes, mens_ano, mens_dt_lancto, mens_valor,"
                        + " escoteiro_esc_cod, categoria_mensalidade_cat_mens_cod) VALUES (?, ?, ?, ?, ?, ?, ?)",
                mensalidade.getMens_cod(), mensalidade.getMens_mes(), mensalidade.getMens_ano(),
                mensalidade.getMens_dt_lancto(), mensalidade.getMens_valor(), mensalidade.getEsc_cod(),
                mensalidade.getCat_mensalidade())
                && conexao.manipular("INSERT INTO contas_receber (cr_cod, cr_valor, cr_desc, atividade_especial_atv_cod,"
                        + " mensalidade_mens_cod) VALUES (?, ?, ?, ?, ?)",
                mensalidade.getCr_cod(), mensalidade.getMens_valor(), descricao, ATIVIDADE_MENSALIDADE,
                mensalidade.getMens_cod())
                && conexao.manipular("INSERT INTO pagamento (pg_cod, pg_dt_pagto, pg_dt_vencto, pg_desc,"
                        + " tipo_pagamento_tp_cod, contas_receber_cr_cod, pg_valor_pago) VALUES (?, NULL, ?, ?, ?, ?, 0)",
                ultimoPagamento + 1, mensalidade.getDt_vencimento(), descricao, TIPO_PAGAMENTO_PADRAO,
                mensalidade.getCr_cod());
    }

    @Override
    public boolean alterar(Mensalidade mensalidade){
        throw new UnsupportedOperationException("Alteração de mensalidade não implementada");
    }

    @Override
    public boolean apagar(Mensalidade mensalidade){
        throw new UnsupportedOperationException("Exclusão de mensalidade não implementada");
    }

    @Override
    public List<Mensalidade> get(){
        return buscar("");
    }

    @Override
    public List<Mensalidade> get(String filtro){
        throw new UnsupportedOperationException("Filtro de mensalidade não implementado");
    }

    @Override
    public Mensalidade get(int id){
        List<Mensalidade> mensalidades = buscar("AND me.mens_cod = ?", id);
        return mensalidades.isEmpty() ? null : mensalidades.get(0);
    }

    public List<Mensalidade> doEscoteiro(int escCod){
        return buscar("AND me.escoteiro_esc_cod = ?", escCod);
    }

    public boolean existe(int escCod, int mes, int ano){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT 1 FROM mensalidade WHERE escoteiro_esc_cod = ? AND mens_mes = ? AND mens_ano = ?",
                escCod, mes, ano);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    private Mensalidade preencher(ResultSet rs) throws SQLException {
        Mensalidade mensalidade = new Mensalidade();
        mensalidade.setMens_cod(rs.getInt("mens_cod"));
        mensalidade.setMens_mes(rs.getInt("mens_mes"));
        mensalidade.setMens_ano(rs.getInt("mens_ano"));
        mensalidade.setMens_dt_lancto(Datas.ler(rs, "mens_dt_lancto"));
        mensalidade.setMens_valor(dinheiro(rs.getDouble("cr_valor")));
        mensalidade.setEsc_cod(rs.getInt("escoteiro_esc_cod"));
        mensalidade.setCat_mensalidade(rs.getInt("categoria_mensalidade_cat_mens_cod"));
        mensalidade.setCr_cod(rs.getInt("cr_cod"));
        mensalidade.setDt_vencimento(Datas.ler(rs, "vencimento"));
        mensalidade.setValor_pago(dinheiro(rs.getDouble("valor_pago")));
        return mensalidade;
    }

    private BigDecimal dinheiro(double valor){
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private List<Mensalidade> buscar(String condicao, Object... valores){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT me.mens_cod, me.mens_mes, me.mens_ano, me.mens_dt_lancto, me.escoteiro_esc_cod,"
                        + " me.categoria_mensalidade_cat_mens_cod, cr.cr_cod, cr.cr_valor,"
                        + " MIN(p.pg_dt_vencto) AS vencimento, COALESCE(SUM(p.pg_valor_pago), 0) AS valor_pago"
                        + " FROM mensalidade me"
                        + " JOIN contas_receber cr ON cr.mensalidade_mens_cod = me.mens_cod"
                        + " AND cr.atividade_especial_atv_cod = " + ATIVIDADE_MENSALIDADE
                        + " LEFT JOIN pagamento p ON p.contas_receber_cr_cod = cr.cr_cod"
                        + " WHERE 1 = 1 " + condicao
                        + " GROUP BY me.mens_cod, cr.cr_cod"
                        + " ORDER BY me.mens_ano, me.mens_mes, me.mens_cod", valores);
        List<Mensalidade> mensalidades = new ArrayList<>();
        try {
            while (rs != null && rs.next())
                mensalidades.add(preencher(rs));
        } catch (SQLException sqlex) {
            mensalidades.clear();
        }
        return mensalidades;
    }
}
