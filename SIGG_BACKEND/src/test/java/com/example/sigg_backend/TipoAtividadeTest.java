package com.example.sigg_backend;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

// Tipos de atividade: tabela tipo_atividade (ta_cod, ta_desc) com os 3 tipos iniciais do dump.
// @Transactional: tudo que o teste gravar é desfeito no fim.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TipoAtividadeTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private ResultActions enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder req, String json) throws Exception {
        return mvc.perform(req.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private int contar(String sql, Object... valores) {
        return jdbc.queryForObject(sql, Integer.class, valores);
    }

    @Test
    void listarTrazOsTresTiposIniciaisDoDump() throws Exception {
        mvc.perform(get("/tipos-atividade"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].ta_desc", containsInAnyOrder("Acampamento", "Excursão / Passeio", "Jamboree / Evento Regional")));
    }

    @Test
    void filtrarPorDescricaoIgnoraMaiusculasEBuscaNoMeio() throws Exception {
        mvc.perform(get("/tipos-atividade").param("filtro", "passeio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ta_desc").value("Excursão / Passeio"));
    }

    @Test
    void buscarPorCodigoDevolveOTipoEInexistenteDevolve400() throws Exception {
        mvc.perform(get("/tipos-atividade/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ta_cod").value(1))
                .andExpect(jsonPath("$.ta_desc").value("Acampamento"));
        mvc.perform(get("/tipos-atividade/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    void cadastrarGravaComOProximoCodigo() throws Exception {
        int proximo = jdbc.queryForObject("SELECT max(ta_cod) FROM tipo_atividade", Integer.class) + 1;

        enviar(post("/tipos-atividade"), "{\"ta_desc\":\"Oficina de Nós\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());

        assertEquals(1, contar("SELECT count(*) FROM tipo_atividade WHERE ta_cod = ? AND ta_desc = 'Oficina de Nós'", proximo));
    }

    @Test
    void cadastrarComDescricaoRepetidaOuInvalidaDevolve400ENaoGravaNada() throws Exception {
        int antes = contar("SELECT count(*) FROM tipo_atividade");

        enviar(post("/tipos-atividade"), "{\"ta_desc\":\"acampamento\"}") // repetida, sem diferenciar maiúsculas
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());
        enviar(post("/tipos-atividade"), "{\"ta_desc\":\"   \"}")
                .andExpect(status().isBadRequest());
        enviar(post("/tipos-atividade"), "{}")
                .andExpect(status().isBadRequest());
        enviar(post("/tipos-atividade"), "{\"ta_desc\":\"" + "x".repeat(101) + "\"}") // ta_desc é VARCHAR(100)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());

        assertEquals(antes, contar("SELECT count(*) FROM tipo_atividade"));
    }

    @Test
    void alterarMudaADescricaoEBarraDescricaoDeOutroTipo() throws Exception {
        enviar(put("/tipos-atividade/1"), "{\"ta_desc\":\"Acampamento de Verão\"}")
                .andExpect(status().isOk());
        assertEquals(1, contar("SELECT count(*) FROM tipo_atividade WHERE ta_cod = 1 AND ta_desc = 'Acampamento de Verão'"));

        enviar(put("/tipos-atividade/1"), "{\"ta_desc\":\"Excursão / Passeio\"}") // já é do tipo 2
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());

        enviar(put("/tipos-atividade/2"), "{\"ta_desc\":\"Excursão / Passeio\"}") // a própria descrição não conta como repetida
                .andExpect(status().isOk());
    }

    @Test
    void apagarRemoveOTipoSemAtividadeVinculada() throws Exception {
        mvc.perform(delete("/tipos-atividade/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
        assertEquals(0, contar("SELECT count(*) FROM tipo_atividade WHERE ta_cod = 3"));
    }

    @Test
    void apagarTipoComAtividadeVinculadaDevolve400() throws Exception {
        jdbc.update("UPDATE atividade_especial SET tipo_atividade_ta_cod = 1 WHERE atv_cod = 6"); // Passeio ao Zoológico

        mvc.perform(delete("/tipos-atividade/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    void apagarTipoInexistenteDevolve400() throws Exception {
        mvc.perform(delete("/tipos-atividade/999"))
                .andExpect(status().isBadRequest());
    }
}
