package com.example.sigg_backend.models;

import com.example.sigg_backend.db.dao.ParametrizacaoDAO;
import com.example.sigg_backend.db.util.Conexao;

// Parametrização do sistema: nome, logo e informações do grupo, guardados no banco (tabela parametrizacao,
// linha única) para que possam ser trocados sem mexer no código.
public class Parametrizacao {

    // Logo como data URL. ~700 mil caracteres em base64 ≈ 500 KB de imagem.
    public static final int TAMANHO_MAXIMO_LOGO = 700_000;
    private static final String PREFIXO_LOGO = "^data:image/(png|jpeg|webp|gif);base64,[A-Za-z0-9+/=]+$";

    private String sigla;
    private String nomeSistema;
    private String slogan;
    private String nomeGrupo;
    private String endereco;
    private String cidade;
    private String estado;
    private String telefone;
    private String email;
    private String logo;

    public Parametrizacao() {
    }

    // Deixa os textos sem espaços nas pontas e troca texto vazio por null nos campos opcionais.
    public void normalizar() {
        sigla = aparar(sigla);
        nomeSistema = aparar(nomeSistema);
        slogan = aparar(slogan);
        nomeGrupo = aparar(nomeGrupo);
        endereco = aparar(endereco);
        cidade = aparar(cidade);
        estado = estado == null ? null : aparar(estado.toUpperCase());
        telefone = telefone == null ? null : aparar(telefone.replaceAll("\\D", ""));
        email = aparar(email);
        logo = aparar(logo);
    }

    // Devolve o problema encontrado, ou null se estiver tudo certo. Chame normalizar() antes.
    public String validar() {
        if (sigla == null || sigla.length() > 20)
            return "A sigla do sistema é obrigatória (até 20 caracteres)";
        if (nomeSistema == null || nomeSistema.length() > 80)
            return "O nome do sistema é obrigatório (até 80 caracteres)";
        if (nomeGrupo == null || nomeGrupo.length() > 80)
            return "O nome do grupo é obrigatório (até 80 caracteres)";
        if (slogan != null && slogan.length() > 50)
            return "O slogan deve ter até 50 caracteres";
        if (endereco != null && endereco.length() > 150)
            return "O endereço deve ter até 150 caracteres";
        if (cidade != null && cidade.length() > 50)
            return "A cidade deve ter até 50 caracteres";
        if (estado != null && !estado.matches("[A-Z]{2}"))
            return "O estado deve ser a sigla com 2 letras (ex.: SP)";
        if (telefone != null && !telefone.matches("\\d{10,11}"))
            return "O telefone deve ter DDD + número (10 ou 11 dígitos)";
        if (email != null && (email.length() > 60 || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")))
            return "E-mail inválido (até 60 caracteres)";
        if (logo != null) {
            if (logo.length() > TAMANHO_MAXIMO_LOGO)
                return "O logo é grande demais (máximo de 500 KB)";
            if (!logo.matches(PREFIXO_LOGO))
                return "O logo deve ser uma imagem PNG, JPG, WEBP ou GIF";
        }
        return null;
    }

    public Parametrizacao buscar(Conexao conexao) {
        return new ParametrizacaoDAO().get(conexao);
    }

    public boolean alterar(Conexao conexao) {
        return new ParametrizacaoDAO().alterar(conexao, this);
    }

    private static String aparar(String texto) {
        if (texto == null)
            return null;
        String aparado = texto.trim();
        return aparado.isEmpty() ? null : aparado;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getNomeSistema() {
        return nomeSistema;
    }

    public void setNomeSistema(String nomeSistema) {
        this.nomeSistema = nomeSistema;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getNomeGrupo() {
        return nomeGrupo;
    }

    public void setNomeGrupo(String nomeGrupo) {
        this.nomeGrupo = nomeGrupo;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
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

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
