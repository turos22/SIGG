package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.ContribuicaoFacade;
import com.example.sigg_backend.models.RegistroContribuicao;
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
import com.example.sigg_backend.db.util.Conexao;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/contribuicoes")
public class ContribuicaoController {

    private final Conexao conexao;
    private final ContribuicaoFacade facade;

    public ContribuicaoController(Conexao conexao, ContribuicaoFacade facade) {
        this.conexao = conexao;
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(){
        return ResponseEntity.ok(facade.listar(conexao));
    }

    @GetMapping("/anjos/{memCod}")
    public ResponseEntity<Object> listarDoAnjo(@PathVariable int memCod){
        return ResponseEntity.ok(facade.listarDoAnjo(conexao, memCod));
    }

    @GetMapping("/verificacao")
    public ResponseEntity<Object> verificar(@RequestParam("cpfAnjo") String cpfAnjo,
                                            @RequestParam("cpfAdotado") String cpfAdotado){
        return ResponseEntity.ok(facade.verificar(conexao, cpfAnjo, cpfAdotado));
    }

    @PostMapping
    public ResponseEntity<Object> registrar(@RequestBody RegistroContribuicao registro){
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.registrar(conexao, registro));
    }
}
