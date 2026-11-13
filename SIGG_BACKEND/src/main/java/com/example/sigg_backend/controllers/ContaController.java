package com.example.sigg_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigg_backend.facade.ContaFacade;
import com.example.sigg_backend.models.AlteracaoAdmin;
import com.example.sigg_backend.models.AlteracaoSituacao;
import com.example.sigg_backend.models.NovaSenhaAdmin;
import com.example.sigg_backend.db.util.Conexao;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final Conexao conexao;
    private final ContaFacade facade;

    public ContaController(Conexao conexao, ContaFacade facade) {
        this.conexao = conexao;
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(){
        return ResponseEntity.ok(facade.listar(conexao));
    }

    @PutMapping("/{memCod}/situacao")
    public ResponseEntity<Object> alterarSituacao(@PathVariable int memCod, @RequestBody AlteracaoSituacao alteracao){
        return ResponseEntity.ok(facade.alterarSituacao(conexao, memCod, alteracao));
    }

    @PutMapping("/{memCod}/admin")
    public ResponseEntity<Object> alterarAdministrador(@PathVariable int memCod, @RequestBody AlteracaoAdmin alteracao){
        return ResponseEntity.ok(facade.alterarAdministrador(conexao, memCod, alteracao));
    }

    @PutMapping("/{memCod}/senha")
    public ResponseEntity<Object> redefinirSenha(@PathVariable int memCod, @RequestBody NovaSenhaAdmin nova){
        return ResponseEntity.ok(facade.redefinirSenha(conexao, memCod, nova));
    }
}
