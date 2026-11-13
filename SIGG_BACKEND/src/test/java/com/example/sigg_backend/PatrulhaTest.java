package com.example.sigg_backend;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

// Escoteiros do dump usados aqui: 2007 (Pedro, patrulha 3, tropa 2) e 2012 (Davi, patrulha 1, tropa 1).
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PatrulhaTest {

    @Autowired
    MockMvc mvc;

    private static String patrulha(String nome, int tropa, Integer lider, Integer sublider) {
        return "{\"nome\":\"" + nome + "\",\"tropa\":{\"cod\":" + tropa + "}"
                + (lider == null ? "" : ",\"lider\":{\"esc_cod\":" + lider + "}")
                + (sublider == null ? "" : ",\"sublider\":{\"esc_cod\":" + sublider + "}") + "}";
    }

    private int criar(String json) throws Exception {
        MvcResult resultado = mvc.perform(post("/patrulhas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.cod");
    }

    @Test
    void listarPorTropaTrazTropaELider() throws Exception {
        mvc.perform(get("/patrulhas?tropa=2")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].cod").value(contains(3, 4, 8)))
                .andExpect(jsonPath("$[0].tropa.nome").value("Tropa Escoteira"))
                .andExpect(jsonPath("$[0].lider.esc_cod").value(2007))
                .andExpect(jsonPath("$[0].lider.membro.nome").value("Pedro Silva Ramos"));
    }

    @Test
    void listarSemFiltroTrazAsNoveDoDump() throws Exception {
        mvc.perform(get("/patrulhas")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(9));
    }

    @Test
    void criarSemLiderEhPermitido() throws Exception {
        MvcResult resultado = mvc.perform(post("/patrulhas").contentType(MediaType.APPLICATION_JSON)
                .content(patrulha("Lobo", 2, null, null))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.cod").value(10)).andReturn();
        String json = resultado.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertNull(JsonPath.read(json, "$.lider"));
        assertNull(JsonPath.read(json, "$.sublider"));
    }

    @Test
    void liderDeOutraTropaERecusado() throws Exception {
        mvc.perform(post("/patrulhas").contentType(MediaType.APPLICATION_JSON).content(patrulha("Lobo", 2, 2012, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Líder deve ser escoteiro de uma patrulha da mesma tropa"));
        mvc.perform(post("/patrulhas").contentType(MediaType.APPLICATION_JSON).content(patrulha("Lobo", 2, 2007, 2012)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Sublíder deve ser escoteiro de uma patrulha da mesma tropa"));
    }

    @Test
    void liderEIgualASublider() throws Exception {
        mvc.perform(post("/patrulhas").contentType(MediaType.APPLICATION_JSON).content(patrulha("Lobo", 2, 2007, 2007)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Líder e sublíder devem ser pessoas diferentes"));
    }

    @Test
    void validaNomeETropa() throws Exception {
        mvc.perform(post("/patrulhas").contentType(MediaType.APPLICATION_JSON).content(patrulha("Lobo 2", 2, null, null)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Nome não pode conter números"));
        mvc.perform(post("/patrulhas").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Lobo\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Tropa é obrigatória"));
        mvc.perform(post("/patrulhas").contentType(MediaType.APPLICATION_JSON).content(patrulha("Lobo", 999, null, null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void alterarTrocaNomeELider() throws Exception {
        int cod = criar(patrulha("Lobo", 2, null, null));
        mvc.perform(put("/patrulhas/" + cod).contentType(MediaType.APPLICATION_JSON)
                .content(patrulha("Lobo Cinza", 2, 2007, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Lobo Cinza"))
                .andExpect(jsonPath("$.lider.esc_cod").value(2007));
        mvc.perform(put("/patrulhas/99999").contentType(MediaType.APPLICATION_JSON).content(patrulha("Lobo", 2, null, null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoExcluiPatrulhaComEscoteiroMasExcluiAVazia() throws Exception {
        mvc.perform(delete("/patrulhas/3")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(containsString("escoteiros")));
        int cod = criar(patrulha("Lobo", 2, null, null));
        mvc.perform(delete("/patrulhas/" + cod)).andExpect(status().isOk());
        mvc.perform(get("/patrulhas/" + cod)).andExpect(status().isNotFound());
    }
}
