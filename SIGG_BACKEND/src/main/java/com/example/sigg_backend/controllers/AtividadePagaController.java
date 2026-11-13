package com.example.sigg_backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigg_backend.facade.AtividadePagaFacade;
import com.example.sigg_backend.models.AtividadePaga;
import com.example.sigg_backend.db.util.Conexao;

// RF_F5 Ofertar Novas Atividades Pagas
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/atividades-pagas")
public class AtividadePagaController {

    private final Conexao conexao;
    private final AtividadePagaFacade facade;

    public AtividadePagaController(Conexao conexao, AtividadePagaFacade facade) {
        this.conexao = conexao;
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(@RequestParam(defaultValue = "false") boolean abertas) {
        return ResponseEntity.ok(facade.listar(conexao, abertas));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Object> buscar(@PathVariable int codigo) {
        return ResponseEntity.ok(facade.buscar(conexao, codigo));
    }

    @PostMapping
    public ResponseEntity<Object> ofertar(@RequestBody AtividadePaga atividade) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.ofertar(conexao, atividade));
    }
}
