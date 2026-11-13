package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.ChefeFacade;
import com.example.sigg_backend.facade.EscoteiroFacade;
import com.example.sigg_backend.facade.PresidenteFacade;
import com.example.sigg_backend.models.Chefe;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Presidente;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.sigg_backend.db.util.Conexao;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/escoteiros")
public class EscoteiroController {

    private final Conexao conexao;
    private final EscoteiroFacade facade;
    private final ChefeFacade chefeFacade;
    private final PresidenteFacade presidenteFacade;

    public EscoteiroController(Conexao conexao, EscoteiroFacade facade, ChefeFacade chefeFacade, PresidenteFacade presidenteFacade) {
        this.conexao = conexao;
        this.facade = facade;
        this.chefeFacade = chefeFacade;
        this.presidenteFacade = presidenteFacade;
    }

    @GetMapping
    public ResponseEntity<Object> listarTodos(@RequestParam(required = false) Integer tropa){
        return ResponseEntity.ok(new Escoteiro().listar(conexao, tropa));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> buscar(@PathVariable int id){
        return ResponseEntity.ok(facade.buscar(conexao, id));
    }

    @PostMapping
    public ResponseEntity<Object> cadastrar(@RequestBody Escoteiro escoteiro){
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.cadastrar(conexao, escoteiro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> alterar(@PathVariable int id, @RequestBody Escoteiro escoteiro){
        return ResponseEntity.ok(facade.alterar(conexao, id, escoteiro));
    }

    @PostMapping("/{id}/chefe")
    public ResponseEntity<Object> promoverAChefe(@PathVariable int id, @RequestBody Chefe dados){
        return ResponseEntity.status(HttpStatus.CREATED).body(chefeFacade.promover(conexao, id, dados));
    }

    @PostMapping("/{id}/presidente")
    public ResponseEntity<Object> promoverAPresidente(@PathVariable int id, @RequestBody Presidente dados){
        return ResponseEntity.status(HttpStatus.CREATED).body(presidenteFacade.promover(conexao, id, dados));
    }
}
