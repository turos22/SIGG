package com.example.sigg_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigg_backend.facade.ParametrizacaoFacade;
import com.example.sigg_backend.models.Parametrizacao;

// GET é público (liberado na ConfiguracaoSeguranca); PUT só para o administrador (conferido na Façade).
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/parametrizacao")
public class ParametrizacaoController {

    private final ParametrizacaoFacade facade;

    public ParametrizacaoController(ParametrizacaoFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> buscar() {
        return ResponseEntity.ok(facade.buscar());
    }

    @PutMapping
    public ResponseEntity<Object> alterar(@RequestBody Parametrizacao parametrizacao) {
        return ResponseEntity.ok(facade.alterar(parametrizacao));
    }
}
