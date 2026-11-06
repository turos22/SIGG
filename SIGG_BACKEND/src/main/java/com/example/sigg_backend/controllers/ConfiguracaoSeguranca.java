package com.example.sigg_backend.controllers;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class ConfiguracaoSeguranca {

    private static final String[] PUBLICAS = {
            "/auth/csrf", "/auth/login", "/auth/logout", "/auth/registro", "/auth/confirmar-conta",
            "/auth/esqueci-senha", "/auth/redefinir-senha", "/health", "/health/**",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain cadeiaDeFiltros(HttpSecurity http,
            @Value("${sigg.seguranca.ativa:true}") boolean ativa) throws Exception {
        http.cors(Customizer.withDefaults());
        if (!ativa) {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(acesso -> acesso.anyRequest().permitAll());
            return http.build();
        }
        http.csrf(csrf -> csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .authorizeHttpRequests(acesso -> acesso
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLICAS).permitAll()
                        .requestMatchers("/auth/**", "/mensalidades/**").authenticated()
                        .requestMatchers("/contas/**").hasRole("ADMIN")
                        .anyRequest().hasAnyRole("ADMIN", "PRESIDENTE", "CHEFE", "FINANCEIRO"))
                .exceptionHandling(erro -> erro.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${sigg.cors.origens:http://localhost:5173}") String origens) {
        List<String> permitidas = Arrays.stream(origens.split(",")).map(String::trim).filter(o -> !o.isEmpty()).toList();
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(permitidas);
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN"));
        configuracao.setAllowCredentials(true);
        configuracao.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);
        return fonte;
    }

    @Bean
    public UserDetailsService semUsuariosLocais() {
        return nome -> {
            throw new UsernameNotFoundException("Autenticação feita pelo AutenticacaoFacade");
        };
    }
}
