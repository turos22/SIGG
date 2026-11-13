package com.example.sigg_backend.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.sigg_backend.db.dao.MembroDAO;
import com.example.sigg_backend.db.util.Conexao;

public class Membro {
    private int mem_cod;
    private String nome;
    private LocalDate dt_nascto;
    private String CPF;
    private String RG;
    private String CEP;
    private String endereco;
    private int endereco_num;
    private String endereco_bairro;
    private String cidade;
    private String estado;
    private String telefone;
    private String email;
    private LocalDate dt_ini;
    private LocalDate dt_fim;
    private List<String> papeis = new ArrayList<>(); // só vem preenchido nas consultas de pessoa

    public Membro() {
    }
    public String validar() {
        if (!texto(nome, 30))
            return "Nome é obrigatório (até 30 caracteres)";
        if (contemDigito(nome))
            return "Nome não pode conter números";
        if (dt_nascto == null || dt_nascto.isAfter(LocalDate.now()))
            return "Data de nascimento inválida";
        if (CPF == null || !CPF.matches("\\d{11}"))
            return "CPF deve ter 11 dígitos, só números";
        if (!Cpf.valido(CPF))
            return "CPF inválido";
        if (!texto(RG, 10))
            return "RG é obrigatório (até 10 caracteres)";
        if (!RG.matches("[0-9X]+"))
            return "RG aceita só números e a letra X";
        if (CEP == null || !CEP.matches("\\d{8}"))
            return "CEP deve ter 8 dígitos, só números";
        if (!texto(endereco, 50) || endereco_num <= 0 || !texto(endereco_bairro, 50) || !texto(cidade, 50) || !texto(estado, 30))
            return "Endereço completo é obrigatório";
        if (contemDigito(endereco_bairro))
            return "Bairro não pode conter números";
        if (contemDigito(cidade))
            return "Cidade não pode conter números";
        if (!Uf.valida(estado))
            return "Estado inválido";
        if (telefone != null && !telefone.matches("\\d{10,11}"))
            return "Telefone deve ter 10 ou 11 dígitos, só números";
        if (email != null && (email.length() > 40 || !email.contains("@")))
            return "E-mail inválido (até 40 caracteres)";
        return null;
    }

    private static boolean contemDigito(String valor) {
        return valor.chars().anyMatch(Character::isDigit);
    }

    private static boolean texto(String valor, int tamanhoMaximo) {
        return valor != null && !valor.isBlank() && valor.length() <= tamanhoMaximo;
    }

    public boolean maiorDeIdade() {
        return dt_nascto != null && !dt_nascto.isAfter(LocalDate.now().minusYears(18));
    }

    public boolean cpfJaCadastrado(Conexao conexao) {
        return new MembroDAO().existeCpf(conexao, CPF);
    }

    public boolean cpfJaCadastradoEmOutro(Conexao conexao) {
        return new MembroDAO().existeCpfDeOutro(conexao, CPF, mem_cod);
    }

    public Membro buscar(Conexao conexao, int memCod) {
        return new MembroDAO().get(conexao, memCod);
    }

    public Membro buscarComPapeis(Conexao conexao, int memCod) {
        return new MembroDAO().getComPapeis(conexao, memCod);
    }

    public List<Membro> listarComPapeis(Conexao conexao, boolean somenteVoluntarios) {
        return new MembroDAO().listarComPapeis(conexao, somenteVoluntarios);
    }

    public boolean gravar(Conexao conexao) {
        return new MembroDAO().gravar(conexao, this);
    }

    public boolean alterar(Conexao conexao) {
        return new MembroDAO().alterar(conexao, this);
    }

    public boolean apagar(Conexao conexao) {
        return new MembroDAO().apagar(conexao, this);
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

    public LocalDate getDt_nascto() {
        return dt_nascto;
    }

    public void setDt_nascto(LocalDate dt_nascto) {
        this.dt_nascto = dt_nascto;
    }

    public String getCPF() {
        return CPF;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }

    public String getRG() {
        return RG;
    }

    public void setRG(String RG) {
        this.RG = RG;
    }

    public String getCEP() {
        return CEP;
    }

    public void setCEP(String CEP) {
        this.CEP = CEP;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public int getEndereco_num() {
        return endereco_num;
    }

    public void setEndereco_num(int endereco_num) {
        this.endereco_num = endereco_num;
    }

    public String getEndereco_bairro() {
        return endereco_bairro;
    }

    public void setEndereco_bairro(String endereco_bairro) {
        this.endereco_bairro = endereco_bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDt_ini() {
        return dt_ini;
    }

    public void setDt_ini(LocalDate dt_ini) {
        this.dt_ini = dt_ini;
    }

    public LocalDate getDt_fim() {
        return dt_fim;
    }

    public void setDt_fim(LocalDate dt_fim) {
        this.dt_fim = dt_fim;
    }

    public List<String> getPapeis() {
        return papeis;
    }

    public void setPapeis(List<String> papeis) {
        this.papeis = papeis;
    }
}
