package com.example.sigg_backend.facade;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.sigg_backend.models.Escoteiros;


@Service
public class EscoteiroFacade {

    public List<Escoteiros> listarTodos() {
        return new Escoteiros().listar();
    }
}
