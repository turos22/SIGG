package com.example.sigg_backend.controllers;

import com.example.sigg_backend.models.Chefe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.sigg_backend.db.util.Conexao;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/chefes")
public class ChefeController {

    private final Conexao conexao;
    public ChefeController(Conexao conexao) {
        this.conexao = conexao;
    }

    @GetMapping
    public ResponseEntity<Object> listar(){
        return ResponseEntity.ok(new Chefe().listarChefes(conexao));
    }
}
