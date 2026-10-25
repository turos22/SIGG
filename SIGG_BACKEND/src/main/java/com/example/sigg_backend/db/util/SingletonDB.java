package com.example.sigg_backend.db.util;

import org.springframework.stereotype.Component;
import javax.sql.DataSource;

@Component
public class SingletonDB {

    private static SingletonDB instancia = null;
    private final Conexao conexao;

    private SingletonDB(DataSource dataSource) {
        conexao = new Conexao(dataSource);
        instancia = this;
    }

    public static Conexao getConexao() {
        if (instancia == null)
            throw new IllegalStateException("SingletonDB ainda não foi inicializado pelo Spring");
        return instancia.conexao;
    }
}