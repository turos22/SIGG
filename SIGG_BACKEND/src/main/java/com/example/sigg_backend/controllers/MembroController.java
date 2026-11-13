package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.EscoteiroFacade;
import com.example.sigg_backend.facade.FinanceiroFacade;
import com.example.sigg_backend.facade.ResponsavelFacade;
import com.example.sigg_backend.models.Escoteiro;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.sigg_backend.db.util.Conexao;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"})
@RestController
@RequestMapping("/membros")
public class MembroController {

    private final Conexao conexao;
    private final EscoteiroFacade escoteiroFacade;
    private final FinanceiroFacade financeiroFacade;
    private final ResponsavelFacade responsavelFacade;

    public MembroController(Conexao conexao, EscoteiroFacade escoteiroFacade, FinanceiroFacade financeiroFacade,
                            ResponsavelFacade responsavelFacade) {
        this.conexao = conexao;
        this.escoteiroFacade = escoteiroFacade;
        this.financeiroFacade = financeiroFacade;
        this.responsavelFacade = responsavelFacade;
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
