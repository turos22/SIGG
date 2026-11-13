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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.facade.RegraNegocioException;
import com.example.sigg_backend.facade.PatrulhaFacade;
import com.example.sigg_backend.models.Patrulha;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/patrulhas")
public class PatrulhaController {

    private final Conexao conexao;
    private final PatrulhaFacade facade;

    public PatrulhaController(Conexao conexao, PatrulhaFacade facade) {
        this.conexao = conexao;
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(@RequestParam(required = false) Integer tropa){
        return ResponseEntity.ok(new Patrulha().listar(conexao, tropa));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> buscar(@PathVariable int id){
        return ResponseEntity.ok(facade.buscar(conexao, id));
    }

    @PostMapping
    public ResponseEntity<Object> cadastrar(@RequestBody Patrulha patrulha){
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.cadastrar(conexao, conferir(patrulha)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> alterar(@PathVariable int id, @RequestBody Patrulha patrulha){
        return ResponseEntity.ok(facade.alterar(conexao, id, conferir(patrulha)));
    }

    private Patrulha conferir(Patrulha patrulha){
        if (patrulha == null)
            throw new RegraNegocioException("Os dados da patrulha são obrigatórios");
        patrulha.normalizar();
        String problema = patrulha.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        return patrulha;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> excluir(@PathVariable int id){
        return ResponseEntity.ok(facade.excluir(conexao, id));
    }
}
