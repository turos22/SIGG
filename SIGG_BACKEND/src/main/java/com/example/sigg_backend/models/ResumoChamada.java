package com.example.sigg_backend.models;

import java.time.LocalDate;
import java.util.List;

import com.example.sigg_backend.db.dao.FrequenciaDAO;
import com.example.sigg_backend.db.util.Conexao;

// Uma chamada já feita, na grade: tropa, data e quantos estavam presentes dos que foram marcados
public class ResumoChamada {

    private Tropa tropa;
    private LocalDate data;
    private int presentes;
    private int total;

    public ResumoChamada() {
    }

    public ResumoChamada(Tropa tropa, LocalDate data, int presentes, int total) {
        this.tropa = tropa;
        this.data = data;
        this.presentes = presentes;
        this.total = total;
    }

    public List<ResumoChamada> listar(Conexao conexao, Integer troCod, int ano) {
        return new FrequenciaDAO().listarResumos(conexao, troCod, ano);
    }

    public Tropa getTropa() {
        return tropa;
    }

    public void setTropa(Tropa tropa) {
        this.tropa = tropa;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public int getPresentes() {
        return presentes;
    }

    public void setPresentes(int presentes) {
        this.presentes = presentes;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
