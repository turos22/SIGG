package com.example.sigg_backend.db.util;

import javax.sql.DataSource;

// Singleton da conexão com o banco: existe uma única instância, criada na primeira vez que é pedida.
// Quem pede é só o InitController, na subida da aplicação; os controllers recebem a instância dele
// e a repassam (como primeiro parâmetro) pela facade, pelo model até a DAO.
public class SingletonDB {

    private static Conexao instancia = null;
    private static DataSource origem = null;

    // Construtor privado: ninguém faz "new SingletonDB()"
    private SingletonDB() {
    }

    // Se a instância ainda não existe, cria; depois devolve sempre a mesma.
    // A segunda condição (pool diferente) só acontece nos testes, em que o Spring sobe um contexto novo,
    // com um pool novo, enquanto o contexto anterior ainda está vivo: a instância antiga apontaria para um pool fechado.
    public static synchronized Conexao getConexao(DataSource dataSource) {
        if (instancia == null || origem != dataSource) {
            instancia = new Conexao(dataSource);
            origem = dataSource;
        }
        return instancia;
    }
}
