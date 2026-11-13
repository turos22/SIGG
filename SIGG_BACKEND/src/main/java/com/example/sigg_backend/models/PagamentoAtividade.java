package com.example.sigg_backend.models;

import com.example.sigg_backend.db.dao.PagamentoAtividadeDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import com.example.sigg_backend.db.util.Conexao;

@SuppressWarnings("unused")
public class PagamentoAtividade {
    private int id;                     // pg_cod
    private int crCod;                  // contas_receber.cr_cod
    private int atvCod;                 // atividade_especial.atv_cod
    private String atividadeNome;       // atividade_especial.atv_nome
    private String atividadeDesc;       // atividade_especial.atv_desc
    private LocalDate atividadeData;    // atividade_especial.atv_data
    private int escCod;                 // escoteiro.esc_cod
    private String escoteiroNome;
    private String escoteiroCpf;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private BigDecimal valorAtividade = BigDecimal.ZERO;
    private BigDecimal valorPago = BigDecimal.ZERO;
    private BigDecimal valorRecebido = BigDecimal.ZERO;
    private int formaPagamentoCod;
    private String formaPagamentoDesc;
    private String situacao;            // ABERTA, PARCIAL, PAGA
    private boolean inadimplente;
    private String observacao;

    public PagamentoAtividade() {
    }

    public BigDecimal saldoRestante() {
        BigDecimal saldo = valorAtividade.subtract(valorPago).setScale(2, RoundingMode.HALF_UP);
        return saldo.signum() < 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : saldo;
    }

    public BigDecimal verificarDivergencia(BigDecimal valorInformado) {
        if (valorInformado == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal base = saldoRestante().compareTo(BigDecimal.ZERO) > 0 ? saldoRestante() : valorAtividade;
        return valorInformado.subtract(base).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean registrar(Conexao conexao) {
        return new PagamentoAtividadeDAO().registrar(conexao, this);
    }

    public boolean estornar(Conexao conexao) {
        return new PagamentoAtividadeDAO().estornar(conexao, this);
    }

    public static PagamentoAtividade buscarPorConta(Conexao conexao, int crCod) {
        return new PagamentoAtividadeDAO().buscarPorConta(conexao, crCod);
    }

    public static PagamentoAtividade[] listarPorEscoteiro(Conexao conexao, int escCod) {
        return new PagamentoAtividadeDAO().listarPorEscoteiro(conexao, escCod);
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

    public int getAtvCod() {
        return atvCod;
    }

    public void setAtvCod(int atvCod) {
        this.atvCod = atvCod;
    }

    public String getAtividadeNome() {
        return atividadeNome;
    }

    public void setAtividadeNome(String atividadeNome) {
        this.atividadeNome = atividadeNome;
    }

    public String getAtividadeDesc() {
        return atividadeDesc;
    }

    public void setAtividadeDesc(String atividadeDesc) {
        this.atividadeDesc = atividadeDesc;
    }

    public LocalDate getAtividadeData() {
        return atividadeData;
    }

    public void setAtividadeData(LocalDate atividadeData) {
        this.atividadeData = atividadeData;
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

    public BigDecimal getValorAtividade() {
        return valorAtividade;
    }

    public void setValorAtividade(BigDecimal valorAtividade) {
        this.valorAtividade = valorAtividade;
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