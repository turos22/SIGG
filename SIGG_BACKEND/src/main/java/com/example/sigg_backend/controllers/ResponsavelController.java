package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.ResponsavelFacade;
import com.example.sigg_backend.models.Responsavel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.sigg_backend.db.util.Conexao;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/responsaveis")
public class ResponsavelController {

    private final Conexao conexao;
    private final ResponsavelFacade facade;

    public ResponsavelController(Conexao conexao, ResponsavelFacade facade) {
        this.conexao = conexao;
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(){
        return ResponseEntity.ok(new Responsavel().listar(conexao));
    }

    // Membro novo + responsável
    @PostMapping
    public ResponseEntity<Object> cadastrar(@RequestBody Responsavel responsavel){
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.cadastrar(conexao, responsavel));
    }
}
