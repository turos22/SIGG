package com.example.sigg_backend.models;

public class RedefinicaoSenha {
    private String token;
    private String senha;

    public RedefinicaoSenha() {
    }

    public String validar() {
        if (token == null || token.isBlank())
            return "Link de redefinição inválido";
        return Conta.erroDeSenha(senha);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
