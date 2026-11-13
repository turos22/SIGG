package com.example.sigg_backend;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.sigg_backend.models.UsuarioLogado;

// Põe um usuário logado no contexto de segurança para testes que chamam a facade direto
// (com "sigg.seguranca.ativa=true"). A conta de mem_cod precisa existir e estar ativa no banco de teste.
final class ComoUsuario {

    private ComoUsuario() {
    }

    static void entrar(int memCod, String... perfis) {
        UsuarioLogado usuario = new UsuarioLogado(memCod, "usuário de teste", List.of(perfis));
        var autoridades = Arrays.stream(perfis).map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil)).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, autoridades));
    }

    static void sair() {
        SecurityContextHolder.clearContext();
    }
}
