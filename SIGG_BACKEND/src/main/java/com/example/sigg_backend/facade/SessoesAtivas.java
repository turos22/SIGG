package com.example.sigg_backend.facade;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Component;

@Component
public class SessoesAtivas {

    private final ObjectProvider<FindByIndexNameSessionRepository<?>> repositorio;

    public SessoesAtivas(ObjectProvider<FindByIndexNameSessionRepository<?>> repositorio) {
        this.repositorio = repositorio;
    }

    public void encerrarTodas(String nomeDoPrincipal) {
        FindByIndexNameSessionRepository<?> sessoes = repositorio.getIfAvailable();
        if (sessoes == null)
            return;
        for (String id : sessoes.findByPrincipalName(nomeDoPrincipal).keySet())
            sessoes.deleteById(id);
    }
}
