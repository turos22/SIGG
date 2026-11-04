package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.ChefeFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/chefes")
public class ChefeController {

    private final ChefeFacade facade;

    public ChefeController(ChefeFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(){
        return ResponseEntity.ok(facade.listar());
    }
}
