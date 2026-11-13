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
        String problema = validarDescricao(tipo.getTa_desc());
        if (problema != null) {
            return ResponseEntity.badRequest().body(Map.of("erro", problema));
        }

        // Validação: Não deixar repetir o nome (Exigência da professora)
        if (TipoAtividade.existeNome(conexao, tipo.getTa_desc(), 0)) {
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
        String problema = validarDescricao(tipo.getTa_desc());
        if (problema != null) {
            return ResponseEntity.badRequest().body(Map.of("erro", problema));
        }

        // Validação: Não deixar repetir o nome para outro registro
        if (TipoAtividade.existeNome(conexao, tipo.getTa_desc(), id)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Já existe outro tipo de atividade com este nome."));
        }

        tipo.setTa_cod(id);
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

    // ta_desc é VARCHAR(100) NOT NULL
    private static String validarDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty())
            return "A descrição do tipo de atividade é obrigatória.";
        if (descricao.trim().length() > 100)
            return "A descrição deve ter até 100 caracteres.";
        return null;
    }
}
