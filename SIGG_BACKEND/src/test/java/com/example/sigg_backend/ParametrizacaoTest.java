package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

// Parametrização do sistema (nome, logo e informações do grupo guardados no banco).
// @Transactional: o que o teste gravar é desfeito no fim.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ParametrizacaoTest {

    // PNG 1x1 transparente
    private static final String LOGO_PNG = "data:image/png;base64,"
            + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private ResultActions alterar(String json) throws Exception {
        return mvc.perform(put("/parametrizacao").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void buscarDevolveOsValoresDoBanco() throws Exception {
        mvc.perform(get("/parametrizacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sigla").value("SIGG"))
                .andExpect(jsonPath("$.nomeSistema").value("Sistema Integrado de Gestão Guayporé"))
                .andExpect(jsonPath("$.logo").doesNotExist());
    }

    @Test
    void alterarGravaNoBancoENormalizaOsCampos() throws Exception {
        alterar("""
                {"sigla":"  GEG ","nomeSistema":"Gestão do Grupo","slogan":"","nomeGrupo":"Grupo Teste",
                 "estado":"sp","telefone":"(18) 3916-2262","email":"grupo@example.com","logo":"%s"}"""
                .formatted(LOGO_PNG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sigla").value("GEG"))
                .andExpect(jsonPath("$.estado").value("SP"))
                .andExpect(jsonPath("$.telefone").value("1839162262"));

        assertEquals("GEG", jdbc.queryForObject("SELECT par_sigla FROM parametrizacao WHERE par_cod = 1", String.class));
        assertEquals(LOGO_PNG, jdbc.queryForObject("SELECT par_logo FROM parametrizacao WHERE par_cod = 1", String.class));
        assertNull(jdbc.queryForObject("SELECT par_slogan FROM parametrizacao WHERE par_cod = 1", String.class));
    }

    @Test
    void campoObrigatorioVazioDevolve400ENaoAltera() throws Exception {
        alterar("{\"sigla\":\"\",\"nomeSistema\":\"X\",\"nomeGrupo\":\"Y\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A sigla do sistema é obrigatória (até 20 caracteres)"));
        assertEquals("SIGG", jdbc.queryForObject("SELECT par_sigla FROM parametrizacao WHERE par_cod = 1", String.class));
    }

    @Test
    void logoQueNaoEImagemEhRecusado() throws Exception {
        alterar("{\"sigla\":\"SIGG\",\"nomeSistema\":\"X\",\"nomeGrupo\":\"Y\",\"logo\":\"data:text/html;base64,PGgxPg==\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O logo deve ser uma imagem PNG, JPG, WEBP ou GIF"));
    }
}
