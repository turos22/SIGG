package com.example.sigg_backend.models;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

public class UsuarioLogado implements Principal, Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ADMIN = "ADMIN";
    public static final String PRESIDENTE = "PRESIDENTE";
    public static final String CHEFE = "CHEFE";
    public static final String FINANCEIRO = "FINANCEIRO";
    public static final String RESPONSAVEL = "RESPONSAVEL";
    public static final String ESCOTEIRO = "ESCOTEIRO";
    public static final List<String> DIRETORIA = List.of(ADMIN, PRESIDENTE, CHEFE, FINANCEIRO);

    private int mem_cod;
    private String nome;
    private ArrayList<String> perfis = new ArrayList<>();

    public UsuarioLogado() {
    }

    public UsuarioLogado(int mem_cod, String nome, List<String> perfis) {
        this.mem_cod = mem_cod;
        this.nome = nome;
        this.perfis = new ArrayList<>(perfis);
    }

    public boolean temPerfil(String perfil) {
        return perfis.contains(perfil);
    }

    public boolean temAlgumPerfil(List<String> procurados) {
        return procurados.stream().anyMatch(perfis::contains);
    }

    public boolean ehDiretoria() {
        return temAlgumPerfil(DIRETORIA);
    }

    public int getMem_cod() {
        return mem_cod;
    }

    public String getNome() {
        return nome;
    }

    public List<String> getPerfis() {
        return perfis;
    }

    @Override
    public String getName() {
        return String.valueOf(mem_cod);
    }
}
