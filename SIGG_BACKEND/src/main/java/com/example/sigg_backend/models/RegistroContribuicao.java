package com.example.sigg_backend.models;

import java.util.ArrayList;
import java.util.List;

public class RegistroContribuicao {
    private String cpf_anjo;
    private List<String> cpfs_adotados = new ArrayList<>();

    public RegistroContribuicao() {
    }

    public String validar() {
        if (cpf_anjo == null || cpf_anjo.isBlank())
            return "CPF do Anjo é obrigatório";
        if (cpfs_adotados == null || cpfs_adotados.isEmpty())
            return "Informe ao menos um adotado";
        return null;
    }

    public String getCpf_anjo() {
        return cpf_anjo;
    }

    public void setCpf_anjo(String cpf_anjo) {
        this.cpf_anjo = cpf_anjo;
    }

    public List<String> getCpfs_adotados() {
        return cpfs_adotados;
    }

    public void setCpfs_adotados(List<String> cpfs_adotados) {
        this.cpfs_adotados = cpfs_adotados;
    }
}
