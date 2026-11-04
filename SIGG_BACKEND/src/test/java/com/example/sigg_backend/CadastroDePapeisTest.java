package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

// Cadastro de membro + papel (escoteiro, financeiro, responsável) e acréscimo de papel a quem já existe.
// @Transactional: tudo que o teste gravar é desfeito no fim, o banco fica como estava.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CadastroDePapeisTest {

    private static final String MEMBRO_NOVO = """
            {"nome":"Lucas Teste","dt_nascto":"2014-05-10","CPF":"52998224725","RG":"123456789","CEP":"19000000",
             "endereco":"Rua Teste","endereco_num":10,"endereco_bairro":"Centro","cidade":"Presidente Prudente",
             "estado":"SP","telefone":"18999990000","email":"lucas@example.com"}""";

    private static final String CPF_DO_PEDRO = "64045110534"; // Pedro Silva Ramos, mem_cod 11, já é escoteiro

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private int maior(String tabela, String coluna) {
        return jdbc.queryForObject("SELECT max(" + coluna + ") FROM " + tabela, Integer.class);
    }

    private int contar(String sql, Object... valores) {
        return jdbc.queryForObject(sql, Integer.class, valores);
    }

    private ResultActions enviar(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void cadastrarEscoteiroGravaOMembroEDepoisOEscoteiro() throws Exception {
        int memCod = maior("membro", "mem_cod") + 1;
        int escCod = maior("escoteiro", "esc_cod") + 1;

        enviar("/escoteiros", "{\"membro\":" + MEMBRO_NOVO + ",\"cat_mensalidade\":1,\"patrulha\":{\"cod\":3}}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membro.mem_cod").value(memCod))
                .andExpect(jsonPath("$.esc_cod").value(escCod));

        Map<String, Object> membro = jdbc.queryForMap(
                "SELECT mem_nome, mem_dt_nascto::text AS nascimento, mem_cpf FROM membro WHERE mem_cod = ?", memCod);
        assertEquals("Lucas Teste", membro.get("mem_nome"));
        assertEquals("2014-05-10", membro.get("nascimento")); // a data não pode mudar de dia por causa de fuso
        assertEquals("52998224725", membro.get("mem_cpf"));

        Map<String, Object> escoteiro = jdbc.queryForMap(
                "SELECT esc_cod, patrulha_pat_cod2, categoria_mensalidade_cat_mens_cod, escoteiro_esc_cod FROM escoteiro WHERE mem_cod = ?", memCod);
        assertEquals(escCod, escoteiro.get("esc_cod"));
        assertEquals(3, escoteiro.get("patrulha_pat_cod2"));
        assertEquals(1, escoteiro.get("categoria_mensalidade_cat_mens_cod"));
        assertEquals(escCod, escoteiro.get("escoteiro_esc_cod")); // sem Anjo: aponta para si mesmo
    }

    @Test
    void cadastrarFinanceiroGravaOMembroEDepoisOFinanceiro() throws Exception {
        int memCod = maior("membro", "mem_cod") + 1;
        int finCod = maior("financeiro", "fin_cod") + 1;

        enviar("/financeiros", "{\"membro\":" + MEMBRO_NOVO + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membro.mem_cod").value(memCod))
                .andExpect(jsonPath("$.fin_cod").value(finCod));

        assertEquals(1, contar("SELECT count(*) FROM membro WHERE mem_cod = ? AND mem_nome = 'Lucas Teste'", memCod));
        assertEquals(1, contar("SELECT count(*) FROM financeiro WHERE mem_cod = ? AND fin_cod = ?", memCod, finCod));
    }

    @Test
    void cadastrarResponsavelGravaOMembroEDepoisOResponsavel() throws Exception {
        int memCod = maior("membro", "mem_cod") + 1;
        int respCod = maior("responsável", "resp_cod") + 1;

        enviar("/responsaveis", "{\"membro\":" + MEMBRO_NOVO + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membro.mem_cod").value(memCod))
                .andExpect(jsonPath("$.resp_cod").value(respCod));

        assertEquals(1, contar("SELECT count(*) FROM responsável WHERE mem_cod = ? AND resp_cod = ?", memCod, respCod));
    }

    @Test
    void acrescentarFinanceiroAMembroExistenteNaoCriaOutroMembro() throws Exception {
        int membrosAntes = contar("SELECT count(*) FROM membro");

        enviar("/membros/9/financeiro", "{}") // Carla Ramos Silva: só responsável
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membro.nome").value("Carla Ramos Silva"));

        assertEquals(membrosAntes, contar("SELECT count(*) FROM membro"));
        assertEquals(1, contar("SELECT count(*) FROM financeiro WHERE mem_cod = 9"));
    }

    @Test
    void acrescentarEscoteiroAResponsavelExistente() throws Exception {
        int membrosAntes = contar("SELECT count(*) FROM membro");

        enviar("/membros/10/escoteiro", "{\"cat_mensalidade\":1,\"patrulha\":{\"cod\":4}}") // Marcos Antônio Silva
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membro.nome").value("Marcos Antônio Silva"));

        assertEquals(membrosAntes, contar("SELECT count(*) FROM membro"));
        assertEquals(1, contar("SELECT count(*) FROM escoteiro WHERE mem_cod = 10 AND patrulha_pat_cod2 = 4"));
    }

    @Test
    void acrescentarResponsavelAEscoteiroExistente() throws Exception {
        int membrosAntes = contar("SELECT count(*) FROM membro");

        enviar("/membros/11/responsavel", "{}") // Pedro Silva Ramos: só escoteiro
                .andExpect(status().isCreated());

        assertEquals(membrosAntes, contar("SELECT count(*) FROM membro"));
        assertEquals(1, contar("SELECT count(*) FROM responsável WHERE mem_cod = 11"));
    }
}
