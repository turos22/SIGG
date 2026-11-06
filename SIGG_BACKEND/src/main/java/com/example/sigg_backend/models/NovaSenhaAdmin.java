package com.example.sigg_backend.models;

public class NovaSenhaAdmin {
    private String senha;

    public NovaSenhaAdmin() {
    }

    public String validar() {
        return Conta.erroDeSenha(senha);
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
