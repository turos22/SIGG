package com.example.sigg_backend.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigg_backend.facade.AutenticacaoFacade;
import com.example.sigg_backend.models.ConfirmacaoConta;
import com.example.sigg_backend.models.CredencialLogin;
import com.example.sigg_backend.models.RedefinicaoSenha;
import com.example.sigg_backend.models.RegistroConta;
import com.example.sigg_backend.models.SolicitacaoRecuperacao;
import com.example.sigg_backend.models.UsuarioLogado;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    private final AutenticacaoFacade facade;
    private final SessaoHttp sessao;

    public AutenticacaoController(AutenticacaoFacade facade, SessaoHttp sessao) {
        this.facade = facade;
        this.sessao = sessao;
    }

    @GetMapping("/csrf")
    public ResponseEntity<Object> csrf(CsrfToken token){
        return ResponseEntity.ok(Map.of("token", token == null ? "" : token.getToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<Object> entrar(@RequestBody CredencialLogin credencial,
                                         HttpServletRequest requisicao, HttpServletResponse resposta){
        UsuarioLogado usuario = facade.autenticar(credencial);
        sessao.iniciar(usuario, requisicao, resposta);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/me")
    public ResponseEntity<Object> usuarioAtual(){
        return ResponseEntity.ok(facade.usuarioAtual());
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> sair(HttpServletRequest requisicao){
        sessao.encerrar(requisicao);
        return ResponseEntity.ok(Map.of("mensagem", "Sessão encerrada"));
    }

    @PostMapping("/registro")
    public ResponseEntity<Object> registrar(@RequestBody RegistroConta registro){
        return ResponseEntity.ok(facade.registrar(registro));
    }

    @PostMapping("/confirmar-conta")
    public ResponseEntity<Object> confirmar(@RequestBody ConfirmacaoConta confirmacao){
        return ResponseEntity.ok(facade.confirmarConta(confirmacao));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<Object> recuperar(@RequestBody SolicitacaoRecuperacao solicitacao){
        return ResponseEntity.ok(facade.solicitarRecuperacao(solicitacao));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Object> redefinir(@RequestBody RedefinicaoSenha redefinicao){
        return ResponseEntity.ok(facade.redefinirSenha(redefinicao));
    }
}
