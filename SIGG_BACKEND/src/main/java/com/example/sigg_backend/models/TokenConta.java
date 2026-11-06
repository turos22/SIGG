package com.example.sigg_backend.models;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import com.example.sigg_backend.db.dao.TokenContaDAO;

public class TokenConta {
    public static final String CONFIRMACAO = "CONFIRMACAO";
    public static final String REDEFINICAO = "REDEFINICAO";
    public static final int HORAS_PARA_CONFIRMAR = 24;
    public static final int MINUTOS_PARA_REDEFINIR = 30;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private int tok_cod;
    private int cta_cod;
    private String hash;
    private String tipo;
    private LocalDateTime expira;
    private LocalDateTime usado_em;

    public TokenConta() {
    }

    public static String gerarTokenPuro() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hashDe(String tokenPuro) {
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(tokenPuro.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public boolean estaValido(LocalDateTime agora) {
        return usado_em == null && expira != null && agora.isBefore(expira);
    }

    public boolean gravar() {
        return new TokenContaDAO().gravar(this);
    }

    public boolean marcarComoUsado(LocalDateTime agora) {
        usado_em = agora;
        return new TokenContaDAO().alterar(this);
    }

    public TokenConta buscarValido(String tokenPuro, String tipoEsperado, LocalDateTime agora) {
        if (tokenPuro == null || tokenPuro.isBlank())
            return null;
        TokenConta token = new TokenContaDAO().doHash(hashDe(tokenPuro.trim()), tipoEsperado);
        return token != null && token.estaValido(agora) ? token : null;
    }

    public boolean invalidarAnteriores(int ctaCod, String tipoDoToken, LocalDateTime agora) {
        return new TokenContaDAO().invalidarAnteriores(ctaCod, tipoDoToken, agora);
    }

    public int getTok_cod() {
        return tok_cod;
    }

    public void setTok_cod(int tok_cod) {
        this.tok_cod = tok_cod;
    }

    public int getCta_cod() {
        return cta_cod;
    }

    public void setCta_cod(int cta_cod) {
        this.cta_cod = cta_cod;
    }

    public String hashDoToken() {
        return hash;
    }

    public void definirHash(String hash) {
        this.hash = hash;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getExpira() {
        return expira;
    }

    public void setExpira(LocalDateTime expira) {
        this.expira = expira;
    }

    public LocalDateTime getUsado_em() {
        return usado_em;
    }

    public void setUsado_em(LocalDateTime usado_em) {
        this.usado_em = usado_em;
    }
}
