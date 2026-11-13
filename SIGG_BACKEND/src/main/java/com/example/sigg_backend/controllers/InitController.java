package com.example.sigg_backend.controllers;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.SingletonDB;

// Roda uma única vez, na subida da aplicação: pede a conexão ao SingletonDB e a entrega ao Spring,
// que injeta essa mesma instância no construtor de todos os controllers.
// O DataSource (pool de conexões) é o do próprio Spring, montado a partir do application.properties.
@Configuration
public class InitController {

    @Bean
    public Conexao conexao(DataSource dataSource) {
        return SingletonDB.getConexao(dataSource);
    }
}
