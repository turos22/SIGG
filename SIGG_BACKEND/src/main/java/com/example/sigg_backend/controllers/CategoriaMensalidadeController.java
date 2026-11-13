package com.example.sigg_backend.controllers;

import com.example.sigg_backend.models.CategoriaMensalidade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.sigg_backend.db.util.Conexao;

@RestController
@RequestMapping("/categorias-mensalidade")
public class CategoriaMensalidadeController {

    private final Conexao conexao;
    public CategoriaMensalidadeController(Conexao conexao) {
        this.conexao = conexao;
    }

    @GetMapping
    public List<CategoriaMensalidade> listar(@RequestParam(required = false) String filtro) {
        return CategoriaMensalidade.listar(conexao, filtro);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable int id) {
        CategoriaMensalidade cat = CategoriaMensalidade.buscarPorId(conexao, id);
        if (cat == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Categoria não encontrada."));
        }
        return ResponseEntity.ok(cat);
    }

    @PostMapping
    public ResponseEntity<Object> gravar(@RequestBody CategoriaMensalidade categoria) {
        if (categoria.getDescricao() == null || categoria.getDescricao().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "A descrição da categoria é obrigatória."));
        }
        if (categoria.getDescricao().trim().length() > 100) { // cat_mens_desc é VARCHAR(100)
            return ResponseEntity.badRequest().body(Map.of("erro", "A descrição deve ter até 100 caracteres."));
        }
        if (categoria.getValor() == null || categoria.getValor().doubleValue() < 0) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O valor informado é inválido."));
        }
        if (categoria.getQtd_irmao() < 0) {
            return ResponseEntity.badRequest().body(Map.of("erro", "A quantidade de irmãos não pode ser negativa."));
        }
        if (CategoriaMensalidade.descricaoJaExiste(conexao, categoria.getDescricao(), 0)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Já existe uma categoria de mensalidade com essa descrição."));
        }

        boolean sucesso = categoria.gravar(conexao);
        if (sucesso) {
            return ResponseEntity.ok(Map.of("mensagem", "Categoria gravada com sucesso!"));
        }
        return ResponseEntity.badRequest().body(Map.of("erro", "Não foi possível gravar a categoria."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> alterar(@PathVariable int id, @RequestBody CategoriaMensalidade categoria) {
        if (categoria.getDescricao() == null || categoria.getDescricao().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "A descrição não pode ficar vazia."));
        }
        if (categoria.getDescricao().trim().length() > 100) { // cat_mens_desc é VARCHAR(100)
            return ResponseEntity.badRequest().body(Map.of("erro", "A descrição deve ter até 100 caracteres."));
        }
        if (categoria.getValor() == null || categoria.getValor().doubleValue() < 0) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O valor informado é inválido."));
        }
        if (categoria.getQtd_irmao() < 0) {
            return ResponseEntity.badRequest().body(Map.of("erro", "A quantidade de irmãos não pode ser negativa."));
        }
        if (CategoriaMensalidade.descricaoJaExiste(conexao, categoria.getDescricao(), id)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Já existe outra categoria de mensalidade com essa descrição."));
        }

        categoria.setCat_mens_cod(id);
        boolean sucesso = categoria.alterar(conexao);
        if (sucesso) {
            return ResponseEntity.ok(Map.of("mensagem", "Categoria alterada com sucesso!"));
        }
        return ResponseEntity.badRequest().body(Map.of("erro", "Não foi possível alterar a categoria."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> apagar(@PathVariable int id) {
        CategoriaMensalidade cat = CategoriaMensalidade.buscarPorId(conexao, id);
        if (cat == null) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Categoria não encontrada."));
        }

        boolean sucesso = cat.apagar(conexao);
        if (sucesso) {
            return ResponseEntity.ok(Map.of("mensagem", "Categoria excluída com sucesso!"));
        }
        return ResponseEntity.badRequest().body(Map.of("erro", "Não é possível excluir esta categoria pois há escoteiros ou mensalidades vinculados a ela."));
    }
}