package com.example.sigg_backend;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

// Dump: escoteiros 11 (Pedro) e 12 (Laura, menor); responsáveis 9 e 10 já ligados a eles; 13 (Fernanda) é
// responsável de 14, 15 e 16 e não tem vínculo com 11 nem 12.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VinculoDeResponsavelTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private ResultActions vincular(int escoteiro, int responsavel) throws Exception {
        return mvc.perform(post("/escoteiros/" + escoteiro + "/responsaveis/" + responsavel));
    }

    private ResultActions desvincular(int escoteiro, int responsavel) throws Exception {
        return mvc.perform(delete("/escoteiros/" + escoteiro + "/responsaveis/" + responsavel));
    }

    private int contarComoResponsavel(int memCod) {
        return jdbc.queryForObject("SELECT count(*) FROM responsável WHERE mem_cod = ?", Integer.class, memCod);
    }

    private int novaPessoaAdulta() throws Exception {
        String json = """
                {"nome":"Gisele Responsavel","dt_nascto":"1985-02-10","CPF":"52998224725","RG":"123456789",
                 "CEP":"19025080","endereco":"Rua Teste","endereco_num":10,"endereco_bairro":"Centro",
                 "cidade":"Presidente Prudente","estado":"SP"}""";
        MvcResult resultado = mvc.perform(post("/membros").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.mem_cod");
    }

    @Test
    void vincularDevolveOsResponsaveisDoEscoteiro() throws Exception {
        vincular(11, 13).andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.membro.mem_cod==13)]").exists());
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM escoteiro_responsavel"
                + " WHERE escoteiro_mem_cod = 11 AND responsável_mem_cod = 13", Integer.class));
    }

    @Test
    void vinculoRepetidoERecusado() throws Exception {
        vincular(11, 9).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(containsString("já é responsável")));
    }

    @Test
    void escoteiroNaoEResponsavelDeSiMesmo() throws Exception {
        vincular(11, 11).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O escoteiro não pode ser responsável por si mesmo"));
    }

    @Test
    void menorDeIdadeNaoPodeSerResponsavel() throws Exception {
        vincular(11, 12).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Responsável deve ter 18 anos ou mais"));
    }

    @Test
    void pessoaGanhaOPapelAoVincularEPerdeAoSairDoUltimoVinculo() throws Exception {
        int memCod = novaPessoaAdulta();
        assertEquals(0, contarComoResponsavel(memCod));
        vincular(11, memCod).andExpect(status().isCreated());
        vincular(12, memCod).andExpect(status().isCreated());
        assertEquals(1, contarComoResponsavel(memCod));

        desvincular(11, memCod).andExpect(status().isOk());
        assertEquals(1, contarComoResponsavel(memCod)); // ainda responde por 12

        desvincular(12, memCod).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.membro.mem_cod==" + memCod + ")]").isEmpty());
        assertEquals(0, contarComoResponsavel(memCod));
    }

    @Test
    void desvincularQuemNaoEstaVinculadoDevolve404() throws Exception {
        desvincular(11, 13).andExpect(status().isNotFound());
    }

    @Test
    void escoteiroOuPessoaInexistenteDevolve404() throws Exception {
        vincular(13, 9).andExpect(status().isNotFound()); // 13 não é escoteira
        vincular(11, 99999).andExpect(status().isNotFound());
    }
}
