package com.example.sigg_backend.models;

import java.time.LocalDate;
import java.util.Set;

import com.example.sigg_backend.db.dao.FrequenciaDAO;
import com.example.sigg_backend.db.util.Conexao;

// Presença de um escoteiro numa data: P presente, F falta, J falta justificada
public class Frequencia {

    public static final Set<String> STATUS_VALIDOS = Set.of("P", "F", "J");

    private int freq_cod;
    private int esc_cod;
    private LocalDate data;
    private String status;

    public Frequencia() {
    }

    public Frequencia(int esc_cod, LocalDate data, String status) {
        this.esc_cod = esc_cod;
        this.data = data;
        this.status = status;
    }

    // Um escoteiro tem no máximo um registro por data: gravar de novo na mesma data troca o status
    public boolean gravar(Conexao conexao) {
        return new FrequenciaDAO().gravar(conexao, this);
    }

    public int getFreq_cod() {
        return freq_cod;
    }

    public void setFreq_cod(int freq_cod) {
        this.freq_cod = freq_cod;
    }

    public int getEsc_cod() {
        return esc_cod;
    }

    public void setEsc_cod(int esc_cod) {
        this.esc_cod = esc_cod;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
