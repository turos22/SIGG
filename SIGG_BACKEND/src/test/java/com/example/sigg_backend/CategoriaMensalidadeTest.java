package com.example.sigg_backend;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

// Categorias de mensalidade: o dump traz as 5 categorias com a coluna cat_mens_desc preenchida.
// @Transactional: tudo que o teste gravar é desfeito no fim.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoriaMensalidadeTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private ResultActions enviar(MockHttpServletRequestBuilder req, String json) throws Exception {
        return mvc.perform(req.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String categoria(String descricao) {
        return "{\"descricao\":\"%s\",\"valor\":55.5,\"qtd_irmao\":1}".formatted(descricao);
    }

    private int contar(String sql, Object... valores) {
        return jdbc.queryForObject(sql, Integer.class, valores);
    }

    @Test
    void listarTrazAsCincoCategoriasComDescricao() throws Exception {
        mvc.perform(get("/categorias-mensalidade"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].descricao").value("Escoteiro sem irmãos no grupo"))
                .andExpect(jsonPath("$[4].descricao").value("Isento (voluntários e diretoria)"));
    }

    @Test
    void todasAsCategoriasDoDumpTemDescricao() {
        assertEquals(0, contar("SELECT count(*) FROM categoria_mensalidade WHERE cat_mens_desc IS NULL OR trim(cat_mens_desc) = ''"));
    }

    @Test
    void cadastrarGravaComOProximoCodigo() throws Exception {
        int proximo = jdbc.queryForObject("SELECT max(cat_mens_cod) FROM categoria_mensalidade", Integer.class) + 1;

        enviar(post("/categorias-mensalidade"), categoria("Bolsista parcial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());

        assertEquals(1, contar("SELECT count(*) FROM categoria_mensalidade WHERE cat_mens_cod = ? AND cat_mens_desc = 'Bolsista parcial'", proximo));
    }

    @Test
    void descricaoRepetidaVaziaOuLongaDevolve400ENaoGravaNada() throws Exception {
        int antes = contar("SELECT count(*) FROM categoria_mensalidade");

        enviar(post("/categorias-mensalidade"), categoria("isento (voluntários e diretoria)")) // repetida, sem diferenciar maiúsculas
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());
        enviar(post("/categorias-mensalidade"), categoria("   "))
                .andExpect(status().isBadRequest());
        enviar(post("/categorias-mensalidade"), categoria("x".repeat(101))) // cat_mens_desc é VARCHAR(100)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());

        assertEquals(antes, contar("SELECT count(*) FROM categoria_mensalidade"));
    }

    @Test
    void alterarMudaADescricaoEBarraDescricaoDeOutraCategoria() throws Exception {
        enviar(put("/categorias-mensalidade/1"), categoria("Sem irmãos"))
                .andExpect(status().isOk());
        assertEquals(1, contar("SELECT count(*) FROM categoria_mensalidade WHERE cat_mens_cod = 1 AND cat_mens_desc = 'Sem irmãos'"));

        enviar(put("/categorias-mensalidade/1"), categoria("Escoteiro com 1 irmão no grupo")) // já é da categoria 2
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }
}
