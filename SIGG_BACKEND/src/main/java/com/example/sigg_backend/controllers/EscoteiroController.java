package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.ChefeFacade;
import com.example.sigg_backend.facade.EscoteiroFacade;
import com.example.sigg_backend.facade.PresidenteFacade;
import com.example.sigg_backend.facade.ResponsavelFacade;
import com.example.sigg_backend.models.Chefe;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Presidente;
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

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/escoteiros")
public class EscoteiroController {

    private final Conexao conexao;
    private final EscoteiroFacade facade;
    private final ChefeFacade chefeFacade;
    private final PresidenteFacade presidenteFacade;
    private final ResponsavelFacade responsavelFacade;

    public EscoteiroController(Conexao conexao, EscoteiroFacade facade, ChefeFacade chefeFacade, PresidenteFacade presidenteFacade,
                               ResponsavelFacade responsavelFacade) {
        this.conexao = conexao;
        this.facade = facade;
        this.chefeFacade = chefeFacade;
        this.presidenteFacade = presidenteFacade;
        this.responsavelFacade = responsavelFacade;
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
        if (escoteiro == null)
            throw new RegraNegocioException("Os dados do escoteiro são obrigatórios");
        if (escoteiro.getMembro() == null)
            throw new RegraNegocioException("Os dados do membro são obrigatórios");
        String problema = escoteiro.getMembro().validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        problema = escoteiro.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.cadastrar(conexao, escoteiro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> alterar(@PathVariable int id, @RequestBody Escoteiro escoteiro){
        if (escoteiro == null)
            throw new RegraNegocioException("Os dados do escoteiro são obrigatórios");
        String problema = escoteiro.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        return ResponseEntity.ok(facade.alterar(conexao, id, escoteiro));
    }

    @PostMapping("/{id}/responsaveis/{memCodResp}")
    public ResponseEntity<Object> vincularResponsavel(@PathVariable int id, @PathVariable int memCodResp){
        return ResponseEntity.status(HttpStatus.CREATED).body(responsavelFacade.vincular(conexao, id, memCodResp));
    }

    @DeleteMapping("/{id}/responsaveis/{memCodResp}")
    public ResponseEntity<Object> desvincularResponsavel(@PathVariable int id, @PathVariable int memCodResp){
        return ResponseEntity.ok(responsavelFacade.desvincular(conexao, id, memCodResp));
    }

    @PostMapping("/{id}/chefe")
    public ResponseEntity<Object> promoverAChefe(@PathVariable int id, @RequestBody Chefe dados){
        if (dados == null)
            throw new RegraNegocioException("Os dados do chefe são obrigatórios");
        String problema = dados.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        return ResponseEntity.status(HttpStatus.CREATED).body(chefeFacade.promover(conexao, id, dados));
    }

    @PostMapping("/{id}/presidente")
    public ResponseEntity<Object> promoverAPresidente(@PathVariable int id, @RequestBody Presidente dados){
        if (dados == null)
            throw new RegraNegocioException("Os dados do presidente são obrigatórios");
        String problema = dados.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        return ResponseEntity.status(HttpStatus.CREATED).body(presidenteFacade.promover(conexao, id, dados));
    }
}
