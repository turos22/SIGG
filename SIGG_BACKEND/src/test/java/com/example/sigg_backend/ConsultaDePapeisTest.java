package com.example.sigg_backend;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

// Listagens só trazem membros ativos (mem_dt_fim nulo). Os valores esperados vêm do dump
// (artefatos/Script_sql_v1.sql) ou de SQL direto no banco, nunca do código testado.
@SpringBootTest
@AutoConfigureMockMvc
class ConsultaDePapeisTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private int contar(String sql) {
        return jdbc.queryForObject(sql, Integer.class);
    }

    @Test
    void listarEscoteirosTrazOsAtivosComMembroEPatrulha() throws Exception {
        int ativos = contar("SELECT count(*) FROM escoteiro e JOIN membro m ON m.mem_cod = e.mem_cod WHERE m.mem_dt_fim IS NULL");

        mvc.perform(get("/escoteiros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(ativos)))
                .andExpect(jsonPath("$[?(@.membro.nome == 'Davi Pires Santos')].esc_cod").value(2012))
                .andExpect(jsonPath("$[?(@.membro.nome == 'Davi Pires Santos')].esc_sit_fin").value(1))
                .andExpect(jsonPath("$[?(@.membro.nome == 'Davi Pires Santos')].patrulha.nome").value("Seis Cinzento"))
                .andExpect(jsonPath("$[?(@.membro.nome == 'Davi Pires Santos')].membro.dt_nascto").value("2017-04-17"))
                .andExpect(jsonPath("$[?(@.membro.nome == 'Anderson Moreira Lopes')]", hasSize(0))); // saiu em 2023
    }

    @Test
    void buscarEscoteiroTrazOsResponsaveis() throws Exception {
        mvc.perform(get("/escoteiros/19")) // Davi Pires Santos
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membro.nome").value("Davi Pires Santos"))
                .andExpect(jsonPath("$.responsaveis[*].membro.nome",
                        containsInAnyOrder("Roberto Carlos Santos", "Juliana Pires Santos")))
                .andExpect(jsonPath("$.responsaveis[*].resp_cod", containsInAnyOrder(6004, 6005)));
    }

    @Test
    void buscarQuemNaoEEscoteiroDevolve404() throws Exception {
        // o 404 é da regra (vem com "erro"), não de rota inexistente
        mvc.perform(get("/escoteiros/4")).andExpect(status().isNotFound()) // Teresa: só financeira
                .andExpect(jsonPath("$.erro").isNotEmpty());
        mvc.perform(get("/escoteiros/999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    void listarChefesTrazDadosDeChefeEscoteiroEMembro() throws Exception {
        mvc.perform(get("/chefes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].membro.nome",
                        containsInAnyOrder("Renata Cavalcante Dias", "Felipe Andrade Souza", "Henrique Barbosa Neves")))
                .andExpect(jsonPath("$[?(@.membro.nome == 'Felipe Andrade Souza')].chefe_cod").value(3003))
                .andExpect(jsonPath("$[?(@.membro.nome == 'Felipe Andrade Souza')].gestao_ini").value("2023-02-04"))
                .andExpect(jsonPath("$[?(@.membro.nome == 'Felipe Andrade Souza')].tropa.nome").value("Tropa Escoteira"))
                .andExpect(jsonPath("$[?(@.membro.nome == 'Felipe Andrade Souza')].esc_cod").value(2005))
                .andExpect(jsonPath("$[?(@.membro.nome == 'Felipe Andrade Souza')].patrulha.cod").value(8));
    }

    @Test
    void listarPresidentesTrazSoOsAtivos() throws Exception {
        mvc.perform(get("/presidentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].membro.nome", containsInAnyOrder("Cláudio Roberto Menezes")))
                .andExpect(jsonPath("$[0].pres_cod").value(4002))
                .andExpect(jsonPath("$[0].gestao_fim").value("2027-12-31"));
    }

    @Test
    void listarFinanceirosTrazSoOsAtivos() throws Exception {
        mvc.perform(get("/financeiros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].membro.nome", containsInAnyOrder("Teresa Cristina Vilela")))
                .andExpect(jsonPath("$[0].fin_cod").value(5002));
    }

    @Test
    void listarResponsaveisTrazOsAtivos() throws Exception {
        int ativos = contar("SELECT count(*) FROM responsável r JOIN membro m ON m.mem_cod = r.mem_cod WHERE m.mem_dt_fim IS NULL");

        mvc.perform(get("/responsaveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(ativos)))
                .andExpect(jsonPath("$[?(@.membro.nome == 'Carla Ramos Silva')].membro.CPF").value("76088162015"));
    }
}
