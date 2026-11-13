package com.example.sigg_backend;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

// Dump: Davi (esc 2012) inadimplente com 3 mensalidades em aberto; Enzo (2018) em dia no cadastro mas com outubro
// vencida (PENDENTE); Pedro (2007) em dia, com a mensalidade 01/2026 paga (conta a receber 1).
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SituacaoFinanceiraTest {

    private static final String MARCAR = "/pagamentos-mensalidade/escoteiros/%d/inadimplente";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private ResultActions marcar(int escCod) throws Exception {
        return mvc.perform(post(MARCAR.formatted(escCod)));
    }

    private int situacaoGravada(int escCod) {
        return jdbc.queryForObject("SELECT esc_sit_fin FROM escoteiro WHERE esc_cod = ?", Integer.class, escCod);
    }

    private static int posto(String situacao) {
        return List.of("INADIMPLENTE", "PENDENTE", "EM_DIA").indexOf(situacao);
    }

    @Test
    void situacoesTrazOsTresEstadosComOsCamposDaGrade() throws Exception {
        mvc.perform(get("/pagamentos-mensalidade/situacoes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.esc_cod==2012)].situacao").value(hasItem("INADIMPLENTE")))
                .andExpect(jsonPath("$[?(@.esc_cod==2012)].mensalidades_abertas").value(hasItem(3)))
                .andExpect(jsonPath("$[?(@.esc_cod==2018)].situacao").value(hasItem("PENDENTE")))
                .andExpect(jsonPath("$[?(@.esc_cod==2018)].mensalidades_abertas").value(hasItem(1)))
                .andExpect(jsonPath("$[?(@.esc_cod==2007)].situacao").value(hasItem("EM_DIA")))
                .andExpect(jsonPath("$[?(@.esc_cod==2007)].mensalidades_abertas").value(hasItem(0)))
                .andExpect(jsonPath("$[?(@.esc_cod==2007)].nome").value(hasItem("Pedro Silva Ramos")))
                .andExpect(jsonPath("$[?(@.esc_cod==2007)].cpf").value(hasItem("64045110534")))
                .andExpect(jsonPath("$[?(@.esc_cod==2007)].tropa").value(hasItem("Tropa Escoteira")))
                .andExpect(jsonPath("$[?(@.esc_cod==2007)].patrulha").value(hasItem("Águia")));
    }

    @Test
    void situacoesVemOrdenadasInadimplentePendenteEmDia() throws Exception {
        String json = mvc.perform(get("/pagamentos-mensalidade/situacoes")).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        List<String> situacoes = JsonPath.read(json, "$[*].situacao");
        assertTrue(situacoes.size() > 10);
        for (int i = 1; i < situacoes.size(); i++)
            assertTrue(posto(situacoes.get(i - 1)) <= posto(situacoes.get(i)), "fora de ordem na posição " + i);
        assertEquals("INADIMPLENTE", situacoes.get(0));
    }

    @Test
    void filtroPorTropaTrazSoOsEscoteirosDela() throws Exception {
        int esperados = jdbc.queryForObject("SELECT count(*) FROM escoteiro e JOIN membro m ON m.mem_cod = e.mem_cod"
                + " JOIN patrulha p ON p.pat_cod = e.patrulha_pat_cod2 WHERE p.tropa_tro_cod = 3 AND m.mem_dt_fim IS NULL",
                Integer.class);
        mvc.perform(get("/pagamentos-mensalidade/situacoes?tropa=3")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(esperados))
                .andExpect(jsonPath("$[?(@.tropa != 'Tropa Sênior')]").isEmpty())
                .andExpect(jsonPath("$[*].esc_cod").value(hasItem(2019)));
    }

    @Test
    void marcarPendenteViraInadimplente() throws Exception {
        assertEquals(0, situacaoGravada(2018));
        marcar(2018).andExpect(status().isOk()).andExpect(jsonPath("$.mensagem").exists());
        assertEquals(1, situacaoGravada(2018));
        mvc.perform(get("/pagamentos-mensalidade/situacoes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.esc_cod==2018)].situacao").value(hasItem("INADIMPLENTE")));
    }

    @Test
    void marcarQuemJaEInadimplenteRecusaSemRegravar() throws Exception {
        marcar(2012).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Este escoteiro já está inadimplente"));
        assertEquals(1, situacaoGravada(2012));
    }

    @Test
    void marcarQuemEstaEmDiaRecusa() throws Exception {
        marcar(2007).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro")
                .value("Este escoteiro não possui mensalidades em atraso e não pode ser marcado como Inadimplente"));
        assertEquals(0, situacaoGravada(2007));
    }

    @Test
    void marcarEscoteiroInexistenteDevolve404() throws Exception {
        marcar(99999).andExpect(status().isNotFound());
    }

    @Test
    void estornoReabreAMensalidadeSemMarcarInadimplente() throws Exception {
        mvc.perform(delete("/pagamentos-mensalidade/1/estornar")).andExpect(status().isOk());
        assertEquals(0, situacaoGravada(2007)); // antes do ajuste o estorno gravava 1 sozinho
        mvc.perform(get("/pagamentos-mensalidade/situacoes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.esc_cod==2007)].situacao").value(hasItem("PENDENTE")));
    }
}
