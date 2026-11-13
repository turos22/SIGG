package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// Rotas de remoção de cargo (segurança desligada). Dump: 6 é chefe ativo da tropa 1; 4 é financeira ativa (o 3 foi desligado).
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RemocaoDeCargoTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    @Test
    void deleteChefeEncerraAGestaoSemApagarALinha() throws Exception {
        mvc.perform(delete("/chefes/6")).andExpect(status().isOk()).andExpect(jsonPath("$.mensagem").exists());
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM chefe WHERE mem_cod = 6 AND chefe_gestao_fim = CURRENT_DATE - 1", Integer.class));
        mvc.perform(delete("/chefes/6")).andExpect(status().isNotFound()); // já não é chefe ativo
    }

    @Test
    void deleteFinanceiroApagaALinha() throws Exception {
        mvc.perform(delete("/financeiros/4")).andExpect(status().isOk());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM financeiro WHERE mem_cod = 4", Integer.class));
        mvc.perform(delete("/financeiros/4")).andExpect(status().isNotFound());
    }
}
