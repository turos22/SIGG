package com.example.sigg_backend.controllers;

import com.example.sigg_backend.facade.PagamentoMensalidadeFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/pagamentos-mensalidade")
public class PagamentoMensalidadeController {

    private final PagamentoMensalidadeFacade facade;

    public PagamentoMensalidadeController(PagamentoMensalidadeFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/formas-pagamento")
    public ResponseEntity<Object> listarFormasPagamento() {
        return ResponseEntity.ok(facade.listarFormasPagamento());
    }

    @GetMapping("/escoteiros")
    public ResponseEntity<Object> pesquisarEscoteiros(
            @RequestParam(value = "busca", required = false, defaultValue = "") String busca) {
        return ResponseEntity.ok(facade.pesquisarEscoteiros(busca));
    }

    @GetMapping("/escoteiros/{escCod}")
    public ResponseEntity<Object> consultarSituacao(@PathVariable int escCod) {
        return ResponseEntity.ok(facade.consultarSituacaoEscoteiro(escCod));
    }

    @PostMapping("/{crCod}/registrar")
    public ResponseEntity<Object> registrar(@PathVariable int crCod, @RequestBody Map<String, Object> corpo) {
        BigDecimal valorRecebido = new BigDecimal(corpo.get("valorRecebido").toString());
        int formaPagamentoCod = Integer.parseInt(corpo.get("formaPagamentoCod").toString());
        LocalDate dataPagamento = corpo.get("dataPagamento") != null && !corpo.get("dataPagamento").toString().isBlank()
                ? LocalDate.parse(corpo.get("dataPagamento").toString())
                : LocalDate.now();
        return ResponseEntity.ok(facade.registrarPagamento(crCod, valorRecebido, formaPagamentoCod, dataPagamento));
    }

    @DeleteMapping("/{crCod}/estornar")
    public ResponseEntity<Object> estornar(@PathVariable int crCod) {
        return ResponseEntity.ok(facade.estornarPagamento(crCod));
    }
}