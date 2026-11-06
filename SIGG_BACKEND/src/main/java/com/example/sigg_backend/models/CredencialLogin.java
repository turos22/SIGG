package com.example.sigg_backend.models;

public class CredencialLogin {
    private String cpf;
    private String senha;

    public CredencialLogin() {
    }

    public boolean completa() {
        return cpf != null && senha != null && !senha.isEmpty() && Cpf.somenteDigitos(cpf).length() == 11;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
