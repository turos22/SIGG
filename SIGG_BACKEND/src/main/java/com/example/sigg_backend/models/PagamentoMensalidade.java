package com.example.sigg_backend.models;

import com.example.sigg_backend.db.dao.PagamentoMensalidadeDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import com.example.sigg_backend.db.util.Conexao;

@SuppressWarnings("unused")
public class PagamentoMensalidade {
    private int id;
    private int crCod;
    private int mensCod;
    private int escCod;
    private String escoteiroNome;
    private String escoteiroCpf;
    private String referencia;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private BigDecimal valorDevido = BigDecimal.ZERO;
    private BigDecimal valorPago = BigDecimal.ZERO;
    private BigDecimal valorRecebido = BigDecimal.ZERO;
    private int formaPagamentoCod;
    private String formaPagamentoDesc;
    private String situacao;
    private boolean inadimplente;
    private String observacao;

    public PagamentoMensalidade() {
    }

    public BigDecimal saldoRestante() {
        BigDecimal saldo = valorDevido.subtract(valorPago).setScale(2, RoundingMode.HALF_UP);
        return saldo.signum() < 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : saldo;
    }

    public BigDecimal verificarDivergencia(BigDecimal valorInformado) {
        if (valorInformado == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return valorInformado.subtract(saldoRestante()).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean registrar(Conexao conexao) {
        return new PagamentoMensalidadeDAO().registrar(conexao, this);
    }

    public boolean estornar(Conexao conexao) {
        return new PagamentoMensalidadeDAO().estornar(conexao, this);
    }

    public static PagamentoMensalidade buscarPorConta(Conexao conexao, int crCod) {
        return new PagamentoMensalidadeDAO().buscarPorConta(conexao, crCod);
    }

    public static PagamentoMensalidade[] listarPorEscoteiro(Conexao conexao, int escCod) {
        return new PagamentoMensalidadeDAO().listarPorEscoteiro(conexao, escCod);
    }

    public static FormaPagamento[] listarFormasPagamento(Conexao conexao) {
        return new PagamentoMensalidadeDAO().listarFormasPagamento(conexao);
    }

    public static Escoteiro[] listarEscoteirosComResumo(Conexao conexao) {
        return new PagamentoMensalidadeDAO().listarEscoteirosComResumo(conexao);
    }

    public static boolean possuiMensalidadeVencidaEmAberto(Conexao conexao, int escCod) {
        return new PagamentoMensalidadeDAO().possuiMensalidadeVencidaEmAberto(conexao, escCod);
    }

    public static List<SituacaoEscoteiro> listarSituacoes(Conexao conexao, Integer troCod) {
        return new PagamentoMensalidadeDAO().listarSituacoes(conexao, troCod);
    }

    public static boolean atualizarSituacaoFinanceiraEscoteiro(Conexao conexao, int escCod, int sitFin) {
        return new PagamentoMensalidadeDAO().atualizarSituacaoFinanceiraEscoteiro(conexao, escCod, sitFin);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCrCod() {
        return crCod;
    }

    public void setCrCod(int crCod) {
        this.crCod = crCod;
    }

    public int getMensCod() {
        return mensCod;
    }

    public void setMensCod(int mensCod) {
        this.mensCod = mensCod;
    }

    public int getEscCod() {
        return escCod;
    }

    public void setEscCod(int escCod) {
        this.escCod = escCod;
    }

    public String getEscoteiroNome() {
        return escoteiroNome;
    }

    public void setEscoteiroNome(String escoteiroNome) {
        this.escoteiroNome = escoteiroNome;
    }

    public String getEscoteiroCpf() {
        return escoteiroCpf;
    }

    public void setEscoteiroCpf(String escoteiroCpf) {
        this.escoteiroCpf = escoteiroCpf;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public BigDecimal getValorDevido() {
        return valorDevido;
    }

    public void setValorDevido(BigDecimal valorDevido) {
        this.valorDevido = valorDevido;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }

    public BigDecimal getValorRecebido() {
        return valorRecebido;
    }

    public void setValorRecebido(BigDecimal valorRecebido) {
        this.valorRecebido = valorRecebido;
    }

    public int getFormaPagamentoCod() {
        return formaPagamentoCod;
    }

    public void setFormaPagamentoCod(int formaPagamentoCod) {
        this.formaPagamentoCod = formaPagamentoCod;
    }

    public String getFormaPagamentoDesc() {
        return formaPagamentoDesc;
    }

    public void setFormaPagamentoDesc(String formaPagamentoDesc) {
        this.formaPagamentoDesc = formaPagamentoDesc;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public boolean isInadimplente() {
        return inadimplente;
    }

    public void setInadimplente(boolean inadimplente) {
        this.inadimplente = inadimplente;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public BigDecimal getSaldo() {
        return saldoRestante();
    }
}