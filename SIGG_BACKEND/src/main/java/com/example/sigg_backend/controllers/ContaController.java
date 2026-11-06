package com.example.sigg_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigg_backend.facade.ContaFacade;
import com.example.sigg_backend.models.NovaSenhaAdmin;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaFacade facade;

    public ContaController(ContaFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(){
        return ResponseEntity.ok(facade.listar());
    }

    @PutMapping("/{memCod}/senha")
    public ResponseEntity<Object> redefinirSenha(@PathVariable int memCod, @RequestBody NovaSenhaAdmin nova){
        return ResponseEntity.ok(facade.redefinirSenha(memCod, nova));
    }
}
