package com.example.sigg_backend.db.util;

import java.util.List;

// Toda operação da DAO recebe a Conexao como primeiro parâmetro (criada pelo SingletonDB e repassada pelo controller)
public interface IDAO<T> {
    //Gravar
    public boolean gravar(Conexao conexao, T entidade);
    // Alterar
    public boolean alterar(Conexao conexao, T entidade);
    // Apagar
    public boolean apagar(Conexao conexao, T entidade);
    //Recuperar Todos
    public List<T> get(Conexao conexao);
    //Recuperar por filtros
    public List<T> get(Conexao conexao, String filtro);
    //Recuperar por ID
    public T get(Conexao conexao, int id);
}
