package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.db.dao.FrequenciaDAO;
import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.facade.AcessoNegadoException;
import com.example.sigg_backend.facade.FrequenciaFacade;
import com.example.sigg_backend.facade.NaoEncontradoException;
import com.example.sigg_backend.models.Chamada;
import com.example.sigg_backend.models.ResumoChamada;
import com.example.sigg_backend.models.Tropa;

// Segurança ligada. Contas do dump: 7 = chefe da tropa 2, 2 = presidente, 4 = financeira.
@SpringBootTest(properties = "sigg.seguranca.ativa=true")
@Transactional
class PermissaoDeFrequenciaTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 7);

    @Autowired
    FrequenciaFacade facade;
    @Autowired
    Conexao conexao;
    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void sair() {
        ComoUsuario.sair();
    }

    private Chamada chamada(int tropa) {
        Chamada chamada = new Chamada();
        chamada.setTropa(new Tropa(tropa, null));
        chamada.setData(DIA);
        chamada.setItens(new FrequenciaDAO().itensDaTropa(conexao, tropa, DIA, DIA.getYear()));
        chamada.getItens().forEach(item -> item.setStatus("P"));
        return chamada;
    }

    @Test
    void chefeOperaASuaTropa() {
        ComoUsuario.entrar(7, "CHEFE");
        assertEquals(8, facade.consultar(conexao, 2, DIA).getItens().size());
        assertEquals(8, facade.registrar(conexao, chamada(2)).getItens().size());
        facade.atualizar(conexao, chamada(2));
        assertEquals(8, facade.percentuais(conexao, 2, 2026).size());
        facade.excluir(conexao, 2, DIA);
    }

    @Test
    void chefeNaoOperaOutraTropa() {
        ComoUsuario.entrar(7, "CHEFE");
        assertThrows(AcessoNegadoException.class, () -> facade.consultar(conexao, 1, DIA));
        assertThrows(AcessoNegadoException.class, () -> facade.registrar(conexao, chamada(1)));
        assertThrows(AcessoNegadoException.class, () -> facade.atualizar(conexao, chamada(1)));
        assertThrows(AcessoNegadoException.class, () -> facade.excluir(conexao, 1, LocalDate.of(2026, 8, 1)));
        assertThrows(AcessoNegadoException.class, () -> facade.percentuais(conexao, 1, 2026));
        assertThrows(AcessoNegadoException.class, () -> facade.listar(conexao, 1, 2026));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM frequencia WHERE freq_data = DATE '2026-11-07'", Integer.class));
    }

    @Test
    void listarSemTropaMostraSoAdoChefe() {
        ComoUsuario.entrar(7, "CHEFE");
        var chamadas = facade.listar(conexao, null, 2026);
        assertTrue(!chamadas.isEmpty());
        assertTrue(chamadas.stream().map(ResumoChamada::getTropa).allMatch(tropa -> tropa.getCod() == 2));
    }

    @Test
    void presidenteOperaQualquerTropa() {
        ComoUsuario.entrar(2, "PRESIDENTE");
        assertEquals(10, facade.registrar(conexao, chamada(1)).getItens().size());
        assertTrue(facade.listar(conexao, null, 2026).stream().map(r -> r.getTropa().getCod()).distinct().count() > 1);
    }

    @Test
    void financeiroNaoOperaFrequencia() {
        ComoUsuario.entrar(4, "FINANCEIRO");
        assertThrows(AcessoNegadoException.class, () -> facade.consultar(conexao, 2, DIA));
        assertThrows(AcessoNegadoException.class, () -> facade.listar(conexao, null, 2026));
    }

    // A sessão ainda diz CHEFE (perfil calculado no login), mas a gestão já acabou no banco
    @Test
    void chefeComGestaoEncerradaNaoOperaMesmoComPerfilNaSessao() {
        jdbc.update("UPDATE chefe SET chefe_gestao_fim = CURRENT_DATE - 1 WHERE mem_cod = 7");
        ComoUsuario.entrar(7, "CHEFE");
        assertThrows(AcessoNegadoException.class, () -> facade.consultar(conexao, 2, DIA));
        assertThrows(AcessoNegadoException.class, () -> facade.registrar(conexao, chamada(2)));
        assertThrows(AcessoNegadoException.class, () -> facade.listar(conexao, null, 2026));
    }

    @Test
    void minhaTropaDevolveATropaDoChefeAtivo() {
        ComoUsuario.entrar(7, "CHEFE");
        assertEquals("Tropa Escoteira", facade.minhaTropa(conexao).getNome());
    }

    @Test
    void minhaTropaDevolve404QuemNaoEChefeAtivo() {
        ComoUsuario.entrar(2, "PRESIDENTE");
        assertThrows(NaoEncontradoException.class, () -> facade.minhaTropa(conexao));
        jdbc.update("UPDATE chefe SET chefe_gestao_fim = CURRENT_DATE - 1 WHERE mem_cod = 7");
        ComoUsuario.entrar(7, "CHEFE");
        assertThrows(NaoEncontradoException.class, () -> facade.minhaTropa(conexao));
    }
}
