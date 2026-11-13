package com.example.sigg_backend.models;

import java.util.List;

public final class Uf {

    public static final List<String> SIGLAS = List.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

    private Uf() {
    }

    public static boolean valida(String sigla) {
        return sigla != null && SIGLAS.contains(sigla);
    }
}
