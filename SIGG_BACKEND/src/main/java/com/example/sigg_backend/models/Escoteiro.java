package com.example.sigg_backend.models;

import java.util.ArrayList;
import java.util.List;

import com.example.sigg_backend.db.dao.EscoteiroDAO;
import com.example.sigg_backend.db.util.Conexao;

public class Escoteiro {
    private Membro membro;
    private int esc_cod;
    private int cat_mensalidade;
    private int anjo;
    private Patrulha patrulha;
    private int esc_sit_fin;
    private List<Responsavel> responsaveis = new ArrayList<>();

    public Escoteiro() {
    }

    public String validar() {
        if (patrulha == null || patrulha.getCod() <= 0)
            return "Patrulha é obrigatória";
        return null;
    }

    public boolean gravar(Conexao conexao) {
        return new EscoteiroDAO().gravar(conexao, this);
    }

    public List<Escoteiro> listar(Conexao conexao) {
        return new EscoteiroDAO().get(conexao);
    }

    public List<Escoteiro> listar(Conexao conexao, Integer troCod) {
        return troCod == null ? listar(conexao) : new EscoteiroDAO().daTropa(conexao, troCod);
    }

    public boolean alterarCadastro(Conexao conexao) {
        return new EscoteiroDAO().alterarCadastro(conexao, this);
    }

    public Escoteiro buscar(Conexao conexao, int memCod) {
        return new EscoteiroDAO().get(conexao, memCod);
    }

    public boolean existe(Conexao conexao, int memCod) {
        return new EscoteiroDAO().existe(conexao, memCod);
    }

    public Escoteiro buscarPorCpf(Conexao conexao, String cpf) {
        return new EscoteiroDAO().getPorCpf(conexao, cpf);
    }

    public Escoteiro buscarPorCodigo(Conexao conexao, int escCod) {
        return new EscoteiroDAO().getPorCodigo(conexao, escCod);
    }

    public List<Escoteiro> pesquisar(Conexao conexao, String termo) {
        return new EscoteiroDAO().pesquisar(conexao, termo);
    }

    public List<Escoteiro> doResponsavel(Conexao conexao, int memCodResponsavel) {
        return new EscoteiroDAO().doResponsavel(conexao, memCodResponsavel);
    }

    public List<Escoteiro> adotados(Conexao conexao) {
        return new EscoteiroDAO().doAnjo(conexao, esc_cod);
    }

    public boolean ehAdotado() {
        return anjo > 0 && anjo != esc_cod;
    }

    public boolean vincularAnjo(Conexao conexao, int escCodAnjo) {
        if (!new EscoteiroDAO().vincularAnjo(conexao, membro.getMem_cod(), escCodAnjo))
            return false;
        anjo = escCodAnjo;
        return true;
    }

    public Membro getMembro() {
        return membro;
    }

    public void setMembro(Membro membro) {
        this.membro = membro;
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

    public int getAnjo() {
        return anjo;
    }

    public void setAnjo(int anjo) {
        this.anjo = anjo;
    }

    public Patrulha getPatrulha() {
        return patrulha;
    }

    public void setPatrulha(Patrulha patrulha) {
        this.patrulha = patrulha;
    }

    public int getEsc_sit_fin() {
        return esc_sit_fin;
    }

    public void setEsc_sit_fin(int esc_sit_fin) {
        this.esc_sit_fin = esc_sit_fin;
    }

    public List<Responsavel> getResponsaveis() {
        return responsaveis;
    }

    public void setResponsaveis(List<Responsavel> responsaveis) {
        this.responsaveis = responsaveis;
    }
}
