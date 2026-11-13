package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.facade.AcessoNegadoException;
import com.example.sigg_backend.facade.PagamentoMensalidadeFacade;

// Segurança ligada. Contas do dump: 4 = financeira, 7 = chefe, 13 = responsável. Marcar inadimplência é do financeiro.
@SpringBootTest(properties = "sigg.seguranca.ativa=true")
@Transactional
class PermissaoDeSituacaoFinanceiraTest {

    @Autowired
    PagamentoMensalidadeFacade facade;
    @Autowired
    Conexao conexao;
    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void sair() {
        ComoUsuario.sair();
    }

    @Test
    void financeiroMarcaInadimplente() {
        ComoUsuario.entrar(4, "FINANCEIRO");
        facade.marcarInadimplente(conexao, 2018);
        assertEquals(1, jdbc.queryForObject("SELECT esc_sit_fin FROM escoteiro WHERE esc_cod = 2018", Integer.class));
    }

    @Test
    void chefeEResponsavelNaoMarcamInadimplente() {
        ComoUsuario.entrar(7, "CHEFE");
        assertThrows(AcessoNegadoException.class, () -> facade.marcarInadimplente(conexao, 2018));
        ComoUsuario.entrar(13, "RESPONSAVEL");
        assertThrows(AcessoNegadoException.class, () -> facade.marcarInadimplente(conexao, 2018));
        assertEquals(0, jdbc.queryForObject("SELECT esc_sit_fin FROM escoteiro WHERE esc_cod = 2018", Integer.class));
    }

    @Test
    void responsavelNaoListaSituacoes() {
        ComoUsuario.entrar(13, "RESPONSAVEL");
        assertThrows(AcessoNegadoException.class, () -> facade.listarSituacoes(conexao, null));
    }
}
