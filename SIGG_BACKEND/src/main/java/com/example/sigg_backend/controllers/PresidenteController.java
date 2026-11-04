package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.PresidenteFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// A promoção a presidente fica em POST /escoteiros/{id}/presidente
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/presidentes")
public class PresidenteController {

    private final PresidenteFacade facade;

    public PresidenteController(PresidenteFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(){
        return ResponseEntity.ok(facade.listar());
    }
}
