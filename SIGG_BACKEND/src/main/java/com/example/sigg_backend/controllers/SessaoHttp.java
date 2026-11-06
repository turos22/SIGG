package com.example.sigg_backend.controllers;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import com.example.sigg_backend.models.UsuarioLogado;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class SessaoHttp {

    private final SecurityContextRepository repositorio = new HttpSessionSecurityContextRepository();

    public void iniciar(UsuarioLogado usuario, HttpServletRequest requisicao, HttpServletResponse resposta) {
        if (requisicao.getSession(false) != null)
            requisicao.changeSessionId();
        List<GrantedAuthority> autoridades = usuario.getPerfis().stream()
                .<GrantedAuthority>map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil))
                .toList();
        Authentication autenticacao = UsernamePasswordAuthenticationToken.authenticated(usuario, null, autoridades);
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
        repositorio.saveContext(contexto, requisicao, resposta);
    }

    public void encerrar(HttpServletRequest requisicao) {
        HttpSession sessao = requisicao.getSession(false);
        if (sessao != null)
            sessao.invalidate();
        SecurityContextHolder.clearContext();
    }
}
