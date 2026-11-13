package com.example.sigg_backend.models;

// Linha da grade de situação financeira. EM_DIA e PENDENTE são calculados; INADIMPLENTE só por ação do financeiro.
public class SituacaoEscoteiro {

    public static final String EM_DIA = "EM_DIA";
    public static final String PENDENTE = "PENDENTE";
    public static final String INADIMPLENTE = "INADIMPLENTE";

    private int esc_cod;
    private int mem_cod;
    private String nome;
    private String cpf;
    private String tropa;
    private String patrulha;
    private int mensalidades_abertas;
    private String situacao;

    public static String calcular(int escSitFin, int vencidasEmAberto) {
        if (escSitFin == 1)
            return INADIMPLENTE;
        return vencidasEmAberto > 0 ? PENDENTE : EM_DIA;
    }

    public int getEsc_cod() {
        return esc_cod;
    }

    public void setEsc_cod(int esc_cod) {
        this.esc_cod = esc_cod;
    }

    public int getMem_cod() {
        return mem_cod;
    }

    public void setMem_cod(int mem_cod) {
        this.mem_cod = mem_cod;
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

    public String getTropa() {
        return tropa;
    }

    public void setTropa(String tropa) {
        this.tropa = tropa;
    }

    public String getPatrulha() {
        return patrulha;
    }

    public void setPatrulha(String patrulha) {
        this.patrulha = patrulha;
    }

    public int getMensalidades_abertas() {
        return mensalidades_abertas;
    }

    public void setMensalidades_abertas(int mensalidades_abertas) {
        this.mensalidades_abertas = mensalidades_abertas;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }
}
