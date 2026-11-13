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
import com.example.sigg_backend.facade.PatrulhaFacade;
import com.example.sigg_backend.models.Patrulha;
import com.example.sigg_backend.models.Tropa;

// Segurança ligada: só ADMIN e PRESIDENTE mexem em patrulha. Contas do dump: 2 = presidente, 7 = chefe.
@SpringBootTest(properties = "sigg.seguranca.ativa=true")
@Transactional
class PermissaoDePatrulhaTest {

    @Autowired
    PatrulhaFacade facade;
    @Autowired
    Conexao conexao;

    @AfterEach
    void sair() {
        ComoUsuario.sair();
    }

    private static Patrulha patrulha() {
        Patrulha patrulha = new Patrulha();
        patrulha.setNome("Lobo");
        patrulha.setTropa(new Tropa(2, null));
        return patrulha;
    }

    @Test
    void presidenteCadastraPatrulha() {
        ComoUsuario.entrar(2, "PRESIDENTE");
        assertEquals("Lobo", facade.cadastrar(conexao, patrulha()).getNome());
    }

    @Test
    void chefeNaoCadastraNemAlteraNemExcluiPatrulha() {
        ComoUsuario.entrar(7, "CHEFE");
        assertThrows(AcessoNegadoException.class, () -> facade.cadastrar(conexao, patrulha()));
        assertThrows(AcessoNegadoException.class, () -> facade.alterar(conexao, 3, patrulha()));
        assertThrows(AcessoNegadoException.class, () -> facade.excluir(conexao, 3));
    }
}
