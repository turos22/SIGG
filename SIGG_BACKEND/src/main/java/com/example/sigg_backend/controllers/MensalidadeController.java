package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.MensalidadeFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.sigg_backend.db.util.Conexao;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/mensalidades")
public class MensalidadeController {

    private final Conexao conexao;
    private final MensalidadeFacade facade;

    public MensalidadeController(Conexao conexao, MensalidadeFacade facade) {
        this.conexao = conexao;
        this.facade = facade;
    }

    @GetMapping("/escoteiros")
    public ResponseEntity<Object> pesquisarEscoteiros(@RequestParam("busca") String busca){
        return ResponseEntity.ok(facade.pesquisarEscoteiros(conexao, busca));
    }

    @GetMapping("/escoteiros/{memCod}/saldo")
    public ResponseEntity<Object> calcular(@PathVariable int memCod){
        return ResponseEntity.ok(facade.calcular(conexao, memCod));
    }

    @GetMapping("/responsaveis/{memCod}/dependentes")
    public ResponseEntity<Object> dependentes(@PathVariable int memCod){
        return ResponseEntity.ok(facade.dependentesDoResponsavel(conexao, memCod));
    }

    @GetMapping("/responsaveis/{memCod}/saldo")
    public ResponseEntity<Object> calcularDoResponsavel(@PathVariable int memCod){
        return ResponseEntity.ok(facade.calcularDoResponsavel(conexao, memCod));
    }

    @PostMapping("/lancamentos")
    public ResponseEntity<Object> lancarMes(@RequestParam("mes") int mes, @RequestParam("ano") int ano){
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.lancarMes(conexao, mes, ano));
    }
}
