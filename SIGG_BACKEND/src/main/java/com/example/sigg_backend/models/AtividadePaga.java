package com.example.sigg_backend.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.sigg_backend.db.dao.AtividadePagaDAO;

// RF_F5 Ofertar Novas Atividades Pagas. Uma atividade paga é uma linha de atividade_especial com valor preenchido.
public class AtividadePaga {

    private int codigo;
    private String nome;
    private String descricao;
    private LocalDate data;
    private String local;
    private BigDecimal valor;
    private Integer vagas;
    private LocalDate prazoInteresse;

    public AtividadePaga() {
    }

    // Deixa os textos sem espaços nas pontas; descrição vazia vira null.
    public void normalizar() {
        nome = nome == null ? null : nome.trim();
        local = local == null ? null : local.trim();
        descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
    }

    // Fluxo alternativo 6.1: lista todos os campos obrigatórios que faltaram, de uma vez.
    public String validar() {
        List<String> faltando = new ArrayList<>();
        if (nome == null || nome.isBlank())
            faltando.add("nome");
        if (data == null)
            faltando.add("data de realização");
        if (local == null || local.isBlank())
            faltando.add("local");
        if (valor == null)
            faltando.add("valor");
        if (vagas == null)
            faltando.add("número de vagas");
        if (prazoInteresse == null)
            faltando.add("prazo para manifestação de interesse");
        if (!faltando.isEmpty())
            return "Campos obrigatórios não preenchidos: " + String.join(", ", faltando);

        if (nome.length() > 30)
            return "O nome da atividade deve ter até 30 caracteres";
        if (descricao != null && descricao.length() > 200)
            return "A descrição deve ter até 200 caracteres";
        if (local.length() > 100)
            return "O local deve ter até 100 caracteres";
        if (valor.signum() <= 0)
            return "O valor da atividade paga deve ser maior que zero";
        if (valor.scale() > 2)
            return "O valor deve ter no máximo 2 casas decimais";
        if (vagas <= 0)
            return "O número de vagas deve ser maior que zero";
        if (prazoInteresse.isAfter(data))
            return "O prazo para manifestação de interesse não pode ser depois da data da atividade";
        return null;
    }

    public boolean gravar() {
        return new AtividadePagaDAO().gravar(this);
    }

    public AtividadePaga buscar(int codigo) {
        return new AtividadePagaDAO().get(codigo);
    }

    public List<AtividadePaga> listar(boolean somenteAbertas) {
        return new AtividadePagaDAO().listar(somenteAbertas);
    }

    // Fluxo alternativo 6.3: mesma atividade = mesmo nome (sem diferenciar maiúsculas) e mesma data.
    public boolean jaCadastrada() {
        return new AtividadePagaDAO().existeMesmoNomeEData(nome, data);
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getVagas() {
        return vagas;
    }

    public void setVagas(Integer vagas) {
        this.vagas = vagas;
    }

    public LocalDate getPrazoInteresse() {
        return prazoInteresse;
    }

    public void setPrazoInteresse(LocalDate prazoInteresse) {
        this.prazoInteresse = prazoInteresse;
    }
}
