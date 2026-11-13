package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.FinanceiroFacade;
import com.example.sigg_backend.models.Financeiro;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.facade.RegraNegocioException;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/financeiros")
public class FinanceiroController {

    private final Conexao conexao;
    private final FinanceiroFacade facade;

    public FinanceiroController(Conexao conexao, FinanceiroFacade facade) {
        this.conexao = conexao;
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(){
        return ResponseEntity.ok(new Financeiro().listar(conexao));
    }

    @DeleteMapping("/{memCod}")
    public ResponseEntity<Object> remover(@PathVariable int memCod){
        return ResponseEntity.ok(facade.remover(conexao, memCod));
    }

    @PostMapping
    public ResponseEntity<Object> cadastrar(@RequestBody Financeiro financeiro){
        if (financeiro == null)
            throw new RegraNegocioException("Os dados do financeiro são obrigatórios");
        if (financeiro.getMembro() == null)
            throw new RegraNegocioException("Os dados do membro são obrigatórios");
        String problema = financeiro.getMembro().validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.cadastrar(conexao, financeiro));
    }
}
