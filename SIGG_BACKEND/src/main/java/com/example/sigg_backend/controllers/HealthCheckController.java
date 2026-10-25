package com.example.sigg_backend.controllers;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.SingletonDB;


@CrossOrigin(origins = "http://127.0.0.1:5500")
@RestController
public class HealthCheckController {

    @GetMapping("health")
    public ResponseEntity<Object> getHeathAPI(){
        return ResponseEntity.ok("API SUBIU!!!!");
    }

    @GetMapping("health/db")
    public ResponseEntity<Object> getHealthBanco(){
        Conexao conexao = SingletonDB.getConexao();
        if (conexao.getEstadoConexao())
            return ResponseEntity.ok(Map.of("banco", "UP"));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("banco", "DOWN", "erro", conexao.getMensagemErro()));
    }

    @GetMapping("health/info")
    public ResponseEntity<Object> getHealthInfo(){
        Duration noAr = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
        Map<String, String> resposta = Map.of(
                "java", System.getProperty("java.version"),
                "sistemaOperacional", System.getProperty("os.name"),
                "horarioServidor", LocalDateTime.now().withNano(0).toString(),
                "noArHa", String.format("%dh %02dmin %02ds", noAr.toHours(), noAr.toMinutesPart(), noAr.toSecondsPart()));
        return ResponseEntity.ok(resposta);
    }
}
