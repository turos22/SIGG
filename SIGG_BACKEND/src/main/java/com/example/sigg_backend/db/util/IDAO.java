package com.example.sigg_backend.db.util;

import java.util.List;

public interface IDAO<T> {
    //Gravar
    public boolean gravar(T entidade);
    // Alterar
    public boolean alterar(T entidade);
    // Apagar
    public boolean apagar(T entidade);
    //Recuperar Todos
    public List<T> get();
    //Recuperar por filtros
    public List<T> get(String filtro);
    //Recuperar por ID
    public T get(int id);
}
