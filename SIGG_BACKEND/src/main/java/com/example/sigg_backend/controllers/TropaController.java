package com.example.sigg_backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.facade.TropaFacade;
import com.example.sigg_backend.models.Tropa;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/tropas")
public class TropaController {

    private final Conexao conexao;
    private final TropaFacade facade;

    public TropaController(Conexao conexao, TropaFacade facade) {
        this.conexao = conexao;
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(){
        return ResponseEntity.ok(new Tropa().listar(conexao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> buscar(@PathVariable int id){
        return ResponseEntity.ok(facade.buscar(conexao, id));
    }

    @PostMapping
    public ResponseEntity<Object> cadastrar(@RequestBody Tropa tropa){
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.cadastrar(conexao, tropa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> alterar(@PathVariable int id, @RequestBody Tropa tropa){
        return ResponseEntity.ok(facade.alterar(conexao, id, tropa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> excluir(@PathVariable int id){
        return ResponseEntity.ok(facade.excluir(conexao, id));
    }
}
