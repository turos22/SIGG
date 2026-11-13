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
import com.example.sigg_backend.facade.NaoAutenticadoException;
import com.example.sigg_backend.facade.TropaFacade;
import com.example.sigg_backend.models.Tropa;

// Segurança ligada: só ADMIN e PRESIDENTE mexem em tropa. Contas do dump: 2 = presidente, 7 = chefe.
@SpringBootTest(properties = "sigg.seguranca.ativa=true")
@Transactional
class PermissaoDeTropaTest {

    @Autowired
    TropaFacade facade;
    @Autowired
    Conexao conexao;

    @AfterEach
    void sair() {
        ComoUsuario.sair();
    }

    private static Tropa tropa(String nome) {
        Tropa tropa = new Tropa();
        tropa.setNome(nome);
        return tropa;
    }

    @Test
    void presidenteCadastraTropa() {
        ComoUsuario.entrar(2, "PRESIDENTE");
        assertEquals("Clã Pioneiro", facade.cadastrar(conexao, tropa("Clã Pioneiro")).getNome());
    }

    @Test
    void chefeNaoCadastraNemAlteraNemExcluiTropa() {
        ComoUsuario.entrar(7, "CHEFE");
        assertThrows(AcessoNegadoException.class, () -> facade.cadastrar(conexao, tropa("Clã Pioneiro")));
        assertThrows(AcessoNegadoException.class, () -> facade.alterar(conexao, 1, tropa("Outro Nome")));
        assertThrows(AcessoNegadoException.class, () -> facade.excluir(conexao, 1));
    }

    @Test
    void semLoginNaoMexeEmTropa() {
        assertThrows(NaoAutenticadoException.class, () -> facade.cadastrar(conexao, tropa("Clã Pioneiro")));
    }
}
