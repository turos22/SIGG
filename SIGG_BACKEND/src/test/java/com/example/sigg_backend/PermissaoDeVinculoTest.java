package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.facade.AcessoNegadoException;
import com.example.sigg_backend.facade.ResponsavelFacade;

// Segurança ligada: responsável não gerencia vínculos (conta do dump: 13 = responsável; 2 = presidente).
@SpringBootTest(properties = "sigg.seguranca.ativa=true")
@Transactional
class PermissaoDeVinculoTest {

    @Autowired
    ResponsavelFacade facade;
    @Autowired
    Conexao conexao;

    @AfterEach
    void sair() {
        ComoUsuario.sair();
    }

    @Test
    void responsavelNaoVinculaNemDesvincula() {
        ComoUsuario.entrar(13, "RESPONSAVEL");
        assertThrows(AcessoNegadoException.class, () -> facade.vincular(conexao, 11, 13));
        assertThrows(AcessoNegadoException.class, () -> facade.desvincular(conexao, 11, 9));
    }

    @Test
    void presidenteVincula() {
        ComoUsuario.entrar(2, "PRESIDENTE");
        assertEquals(3, facade.vincular(conexao, 11, 13).size());
    }
}
