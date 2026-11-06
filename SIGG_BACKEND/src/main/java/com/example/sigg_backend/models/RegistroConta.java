package com.example.sigg_backend.models;

import java.time.LocalDate;

public class RegistroConta {
    private String cpf;
    private LocalDate dt_nascto;
    private String email;
    private String senha;

    public RegistroConta() {
    }

    public String validar() {
        if (!Cpf.valido(cpf))
            return "CPF inválido";
        if (dt_nascto == null || dt_nascto.isAfter(LocalDate.now()) || dt_nascto.isBefore(LocalDate.of(1900, 1, 1)))
            return "Data de nascimento inválida";
        if (email == null || email.length() > 40 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            return "E-mail inválido";
        return Conta.erroDeSenha(senha);
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDt_nascto() {
        return dt_nascto;
    }

    public void setDt_nascto(LocalDate dt_nascto) {
        this.dt_nascto = dt_nascto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
