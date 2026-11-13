package com.example.sigg_backend;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.contains;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TropaTest {

    @Autowired
    MockMvc mvc;

    private static String tropa(String nome) {
        return "{\"nome\":\"" + nome + "\"}";
    }

    private int criar(String nome) throws Exception {
        MvcResult resultado = mvc.perform(post("/tropas").contentType(MediaType.APPLICATION_JSON).content(tropa(nome)))
                .andExpect(status().isCreated()).andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.cod");
    }

    @Test
    void listarTrazAsTropasDoGrupoPorNome() throws Exception {
        mvc.perform(get("/tropas")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(contains("Alcateia", "Tropa Escoteira", "Tropa Sênior")));
    }

    @Test
    void criarDevolveCodigoSeguinte() throws Exception {
        mvc.perform(post("/tropas").contentType(MediaType.APPLICATION_JSON).content(tropa("Clã Pioneiro")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cod").value(4))
                .andExpect(jsonPath("$.nome").value("Clã Pioneiro"));
    }

    @Test
    void nomeInvalidoDevolve400ComMensagem() throws Exception {
        String obrigatorio = "Nome da tropa é obrigatório (até 20 caracteres)";
        mvc.perform(post("/tropas").contentType(MediaType.APPLICATION_JSON).content(tropa("  ")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(obrigatorio));
        mvc.perform(post("/tropas").contentType(MediaType.APPLICATION_JSON).content(tropa("N".repeat(21))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value(obrigatorio));
        mvc.perform(post("/tropas").contentType(MediaType.APPLICATION_JSON).content(tropa("Tropa 2")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("Nome não pode conter números"));
    }

    @Test
    void nomeRepetidoIgnoraMaiusculas() throws Exception {
        mvc.perform(post("/tropas").contentType(MediaType.APPLICATION_JSON).content(tropa("alcateia")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Já existe uma tropa com esse nome"));
    }

    @Test
    void alterarMantemOProprioNomeERecusaOdeOutra() throws Exception {
        int cod = criar("Clã Pioneiro");
        mvc.perform(put("/tropas/" + cod).contentType(MediaType.APPLICATION_JSON).content(tropa("Clã Pioneiro")))
                .andExpect(status().isOk());
        mvc.perform(put("/tropas/" + cod).contentType(MediaType.APPLICATION_JSON).content(tropa("Clã Novo")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Clã Novo"));
        mvc.perform(put("/tropas/" + cod).contentType(MediaType.APPLICATION_JSON).content(tropa("Alcateia")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/tropas/99999").contentType(MediaType.APPLICATION_JSON).content(tropa("Qualquer")))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarInexistenteDevolve404() throws Exception {
        mvc.perform(get("/tropas/99999")).andExpect(status().isNotFound());
        mvc.perform(get("/tropas/2")).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Tropa Escoteira"));
    }

    @Test
    void naoExcluiTropaComPatrulhaMasExcluiAVazia() throws Exception {
        mvc.perform(delete("/tropas/2")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(containsString("patrulha")));
        int cod = criar("Clã Pioneiro");
        mvc.perform(delete("/tropas/" + cod)).andExpect(status().isOk()).andExpect(jsonPath("$.mensagem").exists());
        mvc.perform(get("/tropas/" + cod)).andExpect(status().isNotFound());
    }
}
