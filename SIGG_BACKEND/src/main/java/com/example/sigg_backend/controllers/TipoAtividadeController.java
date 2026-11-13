package com.example.sigg_backend.controllers;

import com.example.sigg_backend.models.TipoAtividade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.sigg_backend.db.util.Conexao;

@RestController
@RequestMapping("/tipos-atividade")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5173"}, allowCredentials = "true")
public class TipoAtividadeController {

    private final Conexao conexao;
    public TipoAtividadeController(Conexao conexao) {
        this.conexao = conexao;
    }

    @GetMapping
    public List<TipoAtividade> listar(@RequestParam(required = false) String filtro) {
        return TipoAtividade.listar(conexao, filtro);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable int id) {
        TipoAtividade tipo = TipoAtividade.buscarPorId(conexao, id);
        if (tipo == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Tipo de atividade não encontrado."));
        }
        return ResponseEntity.ok(tipo);
    }

    @PostMapping
    public ResponseEntity<Object> gravar(@RequestBody TipoAtividade tipo) {
        // Validação de inputs inválidos
        if (tipo.getAtv_nome() == null || tipo.getAtv_nome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O nome do tipo de atividade é obrigatório (invalid input)."));
        }
        if (tipo.getAtv_valor() == null || tipo.getAtv_valor().doubleValue() < 0) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O valor informado é inválido."));
        }

        // Validação: Não deixar repetir o nome (Exigência da professora)
        if (TipoAtividade.existeNome(conexao, tipo.getAtv_nome(), 0)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Já existe um tipo de atividade cadastrado com este nome."));
        }

        boolean sucesso = tipo.gravar(conexao);
        if (sucesso) {
            return ResponseEntity.ok(Map.of("mensagem", "Tipo de atividade gravado com sucesso!"));
        }
        return ResponseEntity.badRequest().body(Map.of("erro", "Não foi possível gravar o tipo de atividade."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> alterar(@PathVariable int id, @RequestBody TipoAtividade tipo) {
        if (tipo.getAtv_nome() == null || tipo.getAtv_nome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O nome não pode ficar vazio (invalid input)."));
        }

        // Validação: Não deixar repetir o nome para outro registro
        if (TipoAtividade.existeNome(conexao, tipo.getAtv_nome(), id)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Já existe outro tipo de atividade com este nome."));
        }

        tipo.setAtv_cod(id);
        boolean sucesso = tipo.alterar(conexao);
        if (sucesso) {
            return ResponseEntity.ok(Map.of("mensagem", "Tipo de atividade alterado com sucesso!"));
        }
        return ResponseEntity.badRequest().body(Map.of("erro", "Não foi possível alterar o tipo de atividade."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> apagar(@PathVariable int id) {
        TipoAtividade tipo = TipoAtividade.buscarPorId(conexao, id);
        if (tipo == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Tipo de atividade não encontrado."));
        }

        try {
            boolean sucesso = tipo.apagar(conexao);
            if (sucesso) {
                return ResponseEntity.ok(Map.of("mensagem", "Tipo de atividade excluído com sucesso!"));
            }
            return ResponseEntity.badRequest().body(Map.of("erro", "Não é possível excluir este tipo pois existem atividades vinculadas a ele."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Não é possível excluir este tipo pois existem atividades vinculadas a ele."));
        }
    }
}