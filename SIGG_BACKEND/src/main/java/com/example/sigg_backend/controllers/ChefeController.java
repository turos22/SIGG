package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.ChefeFacade;
import com.example.sigg_backend.models.Chefe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.sigg_backend.db.util.Conexao;

// A promoção a chefe fica em POST /escoteiros/{id}/chefe
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/chefes")
public class ChefeController {

    private final Conexao conexao;
    private final ChefeFacade facade;

    public ChefeController(Conexao conexao, ChefeFacade facade) {
        this.conexao = conexao;
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(){
        return ResponseEntity.ok(new Chefe().listarChefes(conexao));
    }

    @DeleteMapping("/{memCod}")
    public ResponseEntity<Object> encerrar(@PathVariable int memCod){
        return ResponseEntity.ok(facade.encerrar(conexao, memCod));
    }
}
