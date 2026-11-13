package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.EscoteiroFacade;
import com.example.sigg_backend.facade.FinanceiroFacade;
import com.example.sigg_backend.facade.MembroFacade;
import com.example.sigg_backend.facade.ResponsavelFacade;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
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

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/membros")
public class MembroController {

    private final Conexao conexao;
    private final MembroFacade facade;
    private final EscoteiroFacade escoteiroFacade;
    private final FinanceiroFacade financeiroFacade;
    private final ResponsavelFacade responsavelFacade;

    public MembroController(Conexao conexao, MembroFacade facade, EscoteiroFacade escoteiroFacade, FinanceiroFacade financeiroFacade,
                            ResponsavelFacade responsavelFacade) {
        this.conexao = conexao;
        this.facade = facade;
        this.escoteiroFacade = escoteiroFacade;
        this.financeiroFacade = financeiroFacade;
        this.responsavelFacade = responsavelFacade;
    }

    @GetMapping
    public ResponseEntity<Object> listar(@RequestParam(defaultValue = "false") boolean voluntarios){
        return ResponseEntity.ok(new Membro().listarComPapeis(conexao, voluntarios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> buscar(@PathVariable int id){
        return ResponseEntity.ok(facade.buscar(conexao, id));
    }

    @PostMapping
    public ResponseEntity<Object> cadastrar(@RequestBody Membro membro){
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.cadastrar(conexao, membro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> alterar(@PathVariable int id, @RequestBody Membro membro){
        return ResponseEntity.ok(facade.alterar(conexao, id, membro));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> excluir(@PathVariable int id){
        return ResponseEntity.ok(facade.excluir(conexao, id));
    }

    @PostMapping("/{id}/escoteiro")
    public ResponseEntity<Object> acrescentarEscoteiro(@PathVariable int id, @RequestBody Escoteiro dados){
        return ResponseEntity.status(HttpStatus.CREATED).body(escoteiroFacade.acrescentar(conexao, id, dados));
    }

    @PostMapping("/{id}/financeiro")
    public ResponseEntity<Object> acrescentarFinanceiro(@PathVariable int id){
        return ResponseEntity.status(HttpStatus.CREATED).body(financeiroFacade.acrescentar(conexao, id));
    }

    @PostMapping("/{id}/responsavel")
    public ResponseEntity<Object> acrescentarResponsavel(@PathVariable int id){
        return ResponseEntity.status(HttpStatus.CREATED).body(responsavelFacade.acrescentar(conexao, id));
    }
}
