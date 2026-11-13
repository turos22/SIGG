package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

// Escoteiros do dump: mem 11 Pedro (patrulha 3, tropa 2, cat 2) e mem 19 Davi (patrulha 1, tropa 1, inadimplente).
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlteracaoDeEscoteiroTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private ResultActions alterar(int memCod, String json) throws Exception {
        return mvc.perform(put("/escoteiros/" + memCod).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void escoteiroLidoTrazATropaDaPatrulha() throws Exception {
        mvc.perform(get("/escoteiros/11")).andExpect(status().isOk())
                .andExpect(jsonPath("$.patrulha.nome").value("Águia"))
                .andExpect(jsonPath("$.patrulha.tropa.cod").value(2))
                .andExpect(jsonPath("$.patrulha.tropa.nome").value("Tropa Escoteira"));
    }

    @Test
    void tropaVemEmTodasAsListagensDeEscoteiro() throws Exception {
        for (String url : new String[] {"/escoteiros", "/chefes", "/presidentes", "/pagamentos-mensalidade/escoteiros"})
            mvc.perform(get(url)).andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].patrulha.tropa.nome").isNotEmpty());
    }

    @Test
    void alterarPatrulhaMudaATropaEPoupaOResto() throws Exception {
        alterar(19, "{\"cat_mensalidade\":3,\"patrulha\":{\"cod\":5}}").andExpect(status().isOk())
                .andExpect(jsonPath("$.patrulha.cod").value(5))
                .andExpect(jsonPath("$.patrulha.tropa.cod").value(3))
                .andExpect(jsonPath("$.cat_mensalidade").value(3))
                .andExpect(jsonPath("$.membro.nome").value("Davi Pires Santos"));
        assertEquals(1, jdbc.queryForObject("SELECT esc_sit_fin FROM escoteiro WHERE mem_cod = 19", Integer.class));
        assertEquals("Davi Pires Santos", jdbc.queryForObject("SELECT mem_nome FROM membro WHERE mem_cod = 19", String.class));
    }

    @Test
    void patrulhaInexistenteDevolve404() throws Exception {
        alterar(11, "{\"cat_mensalidade\":2,\"patrulha\":{\"cod\":999}}").andExpect(status().isNotFound());
    }

    @Test
    void categoriaInexistenteDevolve400() throws Exception {
        alterar(11, "{\"cat_mensalidade\":99,\"patrulha\":{\"cod\":3}}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Categoria de mensalidade 99 não existe"));
    }

    @Test
    void pessoaQueNaoEEscoteiroDevolve404() throws Exception {
        alterar(13, "{\"cat_mensalidade\":2,\"patrulha\":{\"cod\":3}}").andExpect(status().isNotFound());
    }

    @Test
    void filtroPorTropaTrazSoPatrulhasDela() throws Exception {
        mvc.perform(get("/escoteiros?tropa=1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(jdbc.queryForObject(
                        "SELECT count(*) FROM escoteiro e JOIN membro m ON m.mem_cod = e.mem_cod"
                                + " JOIN patrulha p ON p.pat_cod = e.patrulha_pat_cod2"
                                + " WHERE p.tropa_tro_cod = 1 AND m.mem_dt_fim IS NULL", Integer.class)))
                .andExpect(jsonPath("$[?(@.patrulha.tropa.cod != 1)]").isEmpty());
    }
}
