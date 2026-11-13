package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.db.dao.ContaDAO;
import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.facade.AcessoNegadoException;
import com.example.sigg_backend.facade.ChefeFacade;
import com.example.sigg_backend.facade.FinanceiroFacade;
import com.example.sigg_backend.facade.NaoEncontradoException;
import com.example.sigg_backend.facade.PresidenteFacade;
import com.example.sigg_backend.facade.RegraNegocioException;
import com.example.sigg_backend.models.Chefe;
import com.example.sigg_backend.models.Presidente;
import com.example.sigg_backend.models.Tropa;

// Segurança ligada. Contas do dump: 2 = presidente (gestão 2025-01-01 a 2027-12-31), 7 = chefe da tropa 2,
// 4 = financeiro. Escoteiro usado: 11 Pedro. Os desligados do dump (mem 1 e 3) não servem de dado de teste.
@SpringBootTest(properties = "sigg.seguranca.ativa=true")
@Transactional
class PromocaoComPermissaoTest {

    @Autowired
    ChefeFacade chefeFacade;
    @Autowired
    PresidenteFacade presidenteFacade;
    @Autowired
    FinanceiroFacade financeiroFacade;
    @Autowired
    Conexao conexao;
    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void sair() {
        ComoUsuario.sair();
    }

    private static Chefe chefe(int tropa) {
        Chefe dados = new Chefe();
        dados.setGestao_ini(LocalDate.now());
        dados.setTropa(new Tropa(tropa, null));
        return dados;
    }

    private static Presidente presidente(String posse, String fim) {
        Presidente dados = new Presidente();
        dados.setGestao_ini(LocalDate.parse(posse));
        dados.setGestao_fim(LocalDate.parse(fim));
        return dados;
    }

    private int linhas(String tabela, int memCod) {
        return jdbc.queryForObject("SELECT count(*) FROM " + tabela + " WHERE mem_cod = ?", Integer.class, memCod);
    }

    @Test
    void chefeNaoPromoveNinguem() {
        ComoUsuario.entrar(7, "CHEFE");
        assertThrows(AcessoNegadoException.class, () -> chefeFacade.promover(conexao, 11, chefe(2)));
        assertThrows(AcessoNegadoException.class, () -> presidenteFacade.promover(conexao, 11, presidente("2026-12-01", "2028-12-31")));
        assertThrows(AcessoNegadoException.class, () -> financeiroFacade.acrescentar(conexao, 13));
        assertThrows(AcessoNegadoException.class, () -> chefeFacade.encerrar(conexao, 6));
        assertThrows(AcessoNegadoException.class, () -> financeiroFacade.remover(conexao, 3));
    }

    @Test
    void presidentePromoveEncerraEReativaChefe() {
        ComoUsuario.entrar(2, "PRESIDENTE");
        chefeFacade.promover(conexao, 11, chefe(2));
        assertTrue(new ContaDAO().perfisDoMembro(conexao, 11).contains("CHEFE"));

        chefeFacade.encerrar(conexao, 11);
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM chefe WHERE mem_cod = 11 AND chefe_gestao_fim = CURRENT_DATE - 1", Integer.class));
        assertFalse(new ContaDAO().perfisDoMembro(conexao, 11).contains("CHEFE")); // o perfil some na hora

        chefeFacade.promover(conexao, 11, chefe(3)); // reativa: mesma linha, tropa nova
        assertEquals(1, linhas("chefe", 11));
        assertEquals(3, jdbc.queryForObject("SELECT tropa_tro_cod FROM chefe WHERE mem_cod = 11", Integer.class));
        assertEquals(null, jdbc.queryForObject("SELECT chefe_gestao_fim FROM chefe WHERE mem_cod = 11", java.sql.Date.class));
        assertTrue(new ContaDAO().perfisDoMembro(conexao, 11).contains("CHEFE"));
    }

    @Test
    void chefeAtivoNaoPodeSerPromovidoDeNovoNemEmTropaInexistente() {
        ComoUsuario.entrar(2, "PRESIDENTE");
        assertThrows(RegraNegocioException.class, () -> chefeFacade.promover(conexao, 7, chefe(2))); // já é chefe
        assertThrows(NaoEncontradoException.class, () -> chefeFacade.promover(conexao, 11, chefe(999)));
        assertThrows(NaoEncontradoException.class, () -> chefeFacade.encerrar(conexao, 11)); // não é chefe
    }

    @Test
    void trocaDePosseEncerraOPresidenteAtualNaVesperaDaPosse() {
        ComoUsuario.entrar(2, "PRESIDENTE");
        presidenteFacade.promover(conexao, 11, presidente("2026-12-01", "2028-12-31"));
        assertEquals("2026-11-30", jdbc.queryForObject("SELECT pres_gestao_fim::text FROM presidente WHERE mem_cod = 2", String.class));
        assertEquals("2026-12-01", jdbc.queryForObject("SELECT pres_gestao_ini::text FROM presidente WHERE mem_cod = 11", String.class));
    }

    @Test
    void exPresidenteVoltaPelaMesmaLinha() {
        jdbc.update("INSERT INTO presidente (mem_cod, pres_cod, pres_gestao_ini, pres_gestao_fim)"
                + " VALUES (11, 4003, DATE '2018-01-01', DATE '2020-12-31')");
        ComoUsuario.entrar(2, "PRESIDENTE");
        presidenteFacade.promover(conexao, 11, presidente("2027-01-01", "2029-12-31"));
        assertEquals(1, linhas("presidente", 11));
        assertEquals("2029-12-31", jdbc.queryForObject("SELECT pres_gestao_fim::text FROM presidente WHERE mem_cod = 11", String.class));
        assertEquals("2026-12-31", jdbc.queryForObject("SELECT pres_gestao_fim::text FROM presidente WHERE mem_cod = 2", String.class));
    }

    @Test
    void posseInvalidaOuDoPresidenteAtualERecusada() {
        ComoUsuario.entrar(2, "PRESIDENTE");
        assertThrows(RegraNegocioException.class, () -> presidenteFacade.promover(conexao, 2, presidente("2027-01-01", "2029-12-31"))); // já é
        assertThrows(RegraNegocioException.class, () -> presidenteFacade.promover(conexao, 11, presidente("2024-12-31", "2028-12-31"))); // antes do início do atual
        assertThrows(RegraNegocioException.class, () -> presidenteFacade.promover(conexao, 11, presidente("2028-12-31", "2027-01-01"))); // fim antes do início
    }

    @Test
    void presidentePromoveERemoveFinanceiro() {
        ComoUsuario.entrar(2, "PRESIDENTE");
        financeiroFacade.acrescentar(conexao, 13);
        assertEquals(1, linhas("financeiro", 13));
        financeiroFacade.remover(conexao, 13);
        assertEquals(0, linhas("financeiro", 13));
        assertThrows(NaoEncontradoException.class, () -> financeiroFacade.remover(conexao, 13));
    }
}
