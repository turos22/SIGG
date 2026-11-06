package com.example.sigg_backend.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.sigg_backend.facade.AcessoNegadoException;
import com.example.sigg_backend.facade.NaoAutenticadoException;
import com.example.sigg_backend.facade.NaoEncontradoException;
import com.example.sigg_backend.facade.RegraNegocioException;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Object> regraNegocio(RegraNegocioException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<Object> naoEncontrado(NaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<Object> naoAutenticado(NaoAutenticadoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Object> acessoNegado(AcessoNegadoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", e.getMessage()));
    }
}
