package com.example.sigg_backend.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import com.example.sigg_backend.db.dao.MensalidadeDAO;

public class Mensalidade {
    public static final String PAGA = "PAGA";
    public static final String ATRASADA = "ATRASADA";
    public static final String PARCIAL = "PARCIAL";
    public static final String ABERTA = "ABERTA";

    private int mens_cod;
    private int mens_mes;
    private int mens_ano;
    private LocalDate mens_dt_lancto;
    private BigDecimal mens_valor = BigDecimal.ZERO;
    private int esc_cod;
    private int cat_mensalidade;
    private int cr_cod;
    private LocalDate dt_vencimento;
    private BigDecimal valor_pago = BigDecimal.ZERO;
    private boolean apadrinhada;

    public Mensalidade() {
    }

    public String validar() {
        if (mens_mes < 1 || mens_mes > 12)
            return "Mês da mensalidade deve ser de 1 a 12";
        if (mens_ano < 2000 || mens_ano > 2100)
            return "Ano da mensalidade inválido";
        if (esc_cod <= 0)
            return "Escoteiro da mensalidade é obrigatório";
        if (mens_valor == null || mens_valor.signum() <= 0)
            return "Valor da mensalidade deve ser maior que zero";
        if (cat_mensalidade <= 0)
            return "Categoria de mensalidade é obrigatória";
        return null;
    }

    public String referencia() {
        return String.format("%02d/%04d", mens_mes, mens_ano);
    }

    public BigDecimal saldoReal() {
        BigDecimal saldo = mens_valor.subtract(valor_pago).setScale(2, RoundingMode.HALF_UP);
        return saldo.signum() < 0 ? BigDecimal.ZERO.setScale(2) : saldo;
    }

    public boolean estaEmAberto() {
        return !apadrinhada && saldoReal().signum() > 0;
    }

    public String situacao(LocalDate hoje) {
        if (apadrinhada || saldoReal().signum() == 0)
            return PAGA;
        if (dt_vencimento != null && dt_vencimento.isBefore(hoje))
            return ATRASADA;
        if (valor_pago.signum() > 0)
            return PARCIAL;
        return ABERTA;
    }

    public boolean gravar() {
        return new MensalidadeDAO().gravar(this);
    }

    public List<Mensalidade> doEscoteiro(int escCod) {
        return new MensalidadeDAO().doEscoteiro(escCod);
    }

    public boolean existe(int escCod, int mes, int ano) {
        return new MensalidadeDAO().existe(escCod, mes, ano);
    }

    public int getMens_cod() {
        return mens_cod;
    }

    public void setMens_cod(int mens_cod) {
        this.mens_cod = mens_cod;
    }

    public int getMens_mes() {
        return mens_mes;
    }

    public void setMens_mes(int mens_mes) {
        this.mens_mes = mens_mes;
    }

    public int getMens_ano() {
        return mens_ano;
    }

    public void setMens_ano(int mens_ano) {
        this.mens_ano = mens_ano;
    }

    public LocalDate getMens_dt_lancto() {
        return mens_dt_lancto;
    }

    public void setMens_dt_lancto(LocalDate mens_dt_lancto) {
        this.mens_dt_lancto = mens_dt_lancto;
    }

    public BigDecimal getMens_valor() {
        return mens_valor;
    }

    public void setMens_valor(BigDecimal mens_valor) {
        this.mens_valor = mens_valor;
    }

    public int getEsc_cod() {
        return esc_cod;
    }

    public void setEsc_cod(int esc_cod) {
        this.esc_cod = esc_cod;
    }

    public int getCat_mensalidade() {
        return cat_mensalidade;
    }

    public void setCat_mensalidade(int cat_mensalidade) {
        this.cat_mensalidade = cat_mensalidade;
    }

    public int getCr_cod() {
        return cr_cod;
    }

    public void setCr_cod(int cr_cod) {
        this.cr_cod = cr_cod;
    }

    public LocalDate getDt_vencimento() {
        return dt_vencimento;
    }

    public void setDt_vencimento(LocalDate dt_vencimento) {
        this.dt_vencimento = dt_vencimento;
    }

    public BigDecimal getValor_pago() {
        return valor_pago;
    }

    public void setValor_pago(BigDecimal valor_pago) {
        this.valor_pago = valor_pago;
    }

    public boolean isApadrinhada() {
        return apadrinhada;
    }

    public void setApadrinhada(boolean apadrinhada) {
        this.apadrinhada = apadrinhada;
    }

    public BigDecimal getSaldo() {
        return apadrinhada ? BigDecimal.ZERO.setScale(2) : saldoReal();
    }

    public String getSituacao() {
        return situacao(LocalDate.now());
    }
}
