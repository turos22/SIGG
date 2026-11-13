package com.example.sigg_backend.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.facade.FrequenciaFacade;
import com.example.sigg_backend.models.Chamada;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/frequencias")
public class FrequenciaController {

    private final Conexao conexao;
    private final FrequenciaFacade facade;

    public FrequenciaController(Conexao conexao, FrequenciaFacade facade) {
        this.conexao = conexao;
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(@RequestParam(required = false) Integer tropa,
                                         @RequestParam(required = false) Integer ano){
        return ResponseEntity.ok(facade.listar(conexao, tropa, ano));
    }

    @GetMapping("/chamada")
    public ResponseEntity<Object> consultar(@RequestParam int tropa,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data){
        return ResponseEntity.ok(facade.consultar(conexao, tropa, data));
    }

    @PostMapping("/chamada")
    public ResponseEntity<Object> registrar(@RequestBody Chamada chamada){
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.registrar(conexao, chamada));
    }

    @PutMapping("/chamada")
    public ResponseEntity<Object> atualizar(@RequestBody Chamada chamada){
        return ResponseEntity.ok(facade.atualizar(conexao, chamada));
    }

    @DeleteMapping("/chamada")
    public ResponseEntity<Object> excluir(@RequestParam int tropa,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data){
        return ResponseEntity.ok(facade.excluir(conexao, tropa, data));
    }

    @GetMapping("/percentuais")
    public ResponseEntity<Object> percentuais(@RequestParam int tropa, @RequestParam(required = false) Integer ano){
        return ResponseEntity.ok(facade.percentuais(conexao, tropa, ano));
    }
}
