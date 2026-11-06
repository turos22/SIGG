package com.example.sigg_backend.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaldoMensalidades {
    private int mem_cod;
    private int esc_cod;
    private String nome;
    private String cpf;
    private boolean anjo;
    private boolean adotado;
    private String nome_anjo;
    private boolean isento;
    private String observacao;
    private List<Mensalidade> abertas = new ArrayList<>();
    private List<Mensalidade> pagas = new ArrayList<>();
    private List<SaldoAdotado> adotados = new ArrayList<>();
    private BigDecimal total_proprio = BigDecimal.ZERO.setScale(2);
    private BigDecimal total_adotados = BigDecimal.ZERO.setScale(2);
    private BigDecimal total_a_pagar = BigDecimal.ZERO.setScale(2);

    public SaldoMensalidades() {
    }

    public void identificar(Escoteiro escoteiro) {
        mem_cod = escoteiro.getMembro().getMem_cod();
        esc_cod = escoteiro.getEsc_cod();
        nome = escoteiro.getMembro().getNome();
        cpf = escoteiro.getMembro().getCPF();
    }

    public void apadrinhar(String nomeDoAnjo) {
        adotado = true;
        isento = true;
        nome_anjo = nomeDoAnjo;
        observacao = "Isento por apadrinhamento. As mensalidades são pagas pelo Anjo " + nomeDoAnjo + ".";
    }

    public void calcular(List<Mensalidade> mensalidades, List<SaldoAdotado> saldosDosAdotados) {
        abertas = new ArrayList<>();
        pagas = new ArrayList<>();
        adotados = saldosDosAdotados;
        anjo = !saldosDosAdotados.isEmpty();
        total_proprio = BigDecimal.ZERO.setScale(2);
        total_adotados = BigDecimal.ZERO.setScale(2);
        for (Mensalidade mensalidade : mensalidades) {
            if (isento)
                mensalidade.setApadrinhada(true);
            if (mensalidade.estaEmAberto()) {
                abertas.add(mensalidade);
                total_proprio = total_proprio.add(mensalidade.saldoReal());
            } else
                pagas.add(mensalidade);
        }
        for (SaldoAdotado saldo : saldosDosAdotados)
            total_adotados = total_adotados.add(saldo.getSubtotal());
        total_a_pagar = total_proprio.add(total_adotados);
    }

    public int getMem_cod() {
        return mem_cod;
    }

    public void setMem_cod(int mem_cod) {
        this.mem_cod = mem_cod;
    }

    public int getEsc_cod() {
        return esc_cod;
    }

    public void setEsc_cod(int esc_cod) {
        this.esc_cod = esc_cod;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public boolean isAnjo() {
        return anjo;
    }

    public void setAnjo(boolean anjo) {
        this.anjo = anjo;
    }

    public boolean isAdotado() {
        return adotado;
    }

    public void setAdotado(boolean adotado) {
        this.adotado = adotado;
    }

    public String getNome_anjo() {
        return nome_anjo;
    }

    public void setNome_anjo(String nome_anjo) {
        this.nome_anjo = nome_anjo;
    }

    public boolean isIsento() {
        return isento;
    }

    public void setIsento(boolean isento) {
        this.isento = isento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<Mensalidade> getAbertas() {
        return abertas;
    }

    public void setAbertas(List<Mensalidade> abertas) {
        this.abertas = abertas;
    }

    public List<Mensalidade> getPagas() {
        return pagas;
    }

    public void setPagas(List<Mensalidade> pagas) {
        this.pagas = pagas;
    }

    public List<SaldoAdotado> getAdotados() {
        return adotados;
    }

    public void setAdotados(List<SaldoAdotado> adotados) {
        this.adotados = adotados;
    }

    public BigDecimal getTotal_proprio() {
        return total_proprio;
    }

    public void setTotal_proprio(BigDecimal total_proprio) {
        this.total_proprio = total_proprio;
    }

    public BigDecimal getTotal_adotados() {
        return total_adotados;
    }

    public void setTotal_adotados(BigDecimal total_adotados) {
        this.total_adotados = total_adotados;
    }

    public BigDecimal getTotal_a_pagar() {
        return total_a_pagar;
    }

    public void setTotal_a_pagar(BigDecimal total_a_pagar) {
        this.total_a_pagar = total_a_pagar;
    }
}
