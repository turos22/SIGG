package com.example.sigg_backend.models;

public class SolicitacaoRecuperacao {
    private String cpf;
    private String email;

    public SolicitacaoRecuperacao() {
    }

    public String validar() {
        if (!Cpf.valido(cpf))
            return "CPF inválido";
        if (email == null || email.length() > 40 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            return "E-mail inválido";
        return null;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
