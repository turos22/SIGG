package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.FinanceiroFacade;
import com.example.sigg_backend.models.Financeiro;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/financeiros")
public class FinanceiroController {

    private final FinanceiroFacade facade;

    public FinanceiroController(FinanceiroFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(){
        return ResponseEntity.ok(facade.listar());
    }

    @PostMapping
    public ResponseEntity<Object> cadastrar(@RequestBody Financeiro financeiro){
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.cadastrar(financeiro));
    }
}
