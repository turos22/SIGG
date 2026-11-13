package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

// Pessoa (membro) é cadastro à parte: aparece na lista mesmo sem nenhum papel.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CadastroDePessoaTest {

    private static final String CPF_A = "52998224725";
    private static final String CPF_B = "11144477735";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private static String pessoa(String nome, String cpf, String telefone) {
        return pessoa(nome, cpf, telefone, "1990-05-20");
    }

    private static String pessoa(String nome, String cpf, String telefone, String nascimento) {
        return """
                {"nome":"%s","dt_nascto":"%s","CPF":"%s","RG":"123456789","CEP":"19025080",
                 "endereco":"Rua 7 de Setembro","endereco_num":140,"endereco_bairro":"Jardim Paulista",
                 "cidade":"Presidente Prudente","estado":"SP","telefone":"%s","email":"pessoa@example.com"}
                """.formatted(nome, nascimento, cpf, telefone);
    }

    private int cadastrar(String nome, String cpf) throws Exception {
        return cadastrar(pessoa(nome, cpf, "18999990000"));
    }

    private int cadastrar(String json) throws Exception {
        MvcResult resultado = mvc.perform(post("/membros").contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isCreated()).andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.mem_cod");
    }

    private String listar(String url) throws Exception {
        return mvc.perform(get(url)).andExpect(status().isOk()).andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private List<String> papeisDe(String json, int memCod) {
        return JsonPath.read(json, "$[?(@.mem_cod==" + memCod + ")].papeis[*]");
    }

    @Test
    void listarTrazPessoaSemCargoComVoluntarioSoParaAdulto() throws Exception {
        int adulta = cadastrar("Ana Adulta", CPF_A);
        int menor = cadastrar(pessoa("Davi Menor", CPF_B, "18999990000", "2015-03-10"));
        String json = listar("/membros");
        List<Integer> codigos = JsonPath.read(json, "$[*].mem_cod");
        assertTrue(codigos.contains(adulta));
        assertTrue(codigos.contains(menor));
        assertEquals(List.of("Voluntário"), papeisDe(json, adulta));
        assertEquals(List.of(), papeisDe(json, menor));
    }

    @Test
    void listarMarcaVoluntarioEResponsavel() throws Exception {
        String json = listar("/membros");
        List<String> fernanda = papeisDe(json, 13);
        assertTrue(fernanda.contains("Voluntário"));
        assertTrue(fernanda.contains("Responsável"));
        List<String> pedro = papeisDe(json, 11); // nasceu em 2013
        assertTrue(pedro.contains("Escoteiro"));
        assertFalse(pedro.contains("Voluntário"));
    }

    @Test
    void filtroVoluntariosExcluiMenores() throws Exception {
        String json = listar("/membros?voluntarios=true");
        List<Integer> codigos = JsonPath.read(json, "$[*].mem_cod");
        assertTrue(codigos.contains(13));
        assertFalse(codigos.contains(11));
    }

    @Test
    void buscarPorCodigoTrazPapeisEDevolve404QuandoNaoExiste() throws Exception {
        mvc.perform(get("/membros/13")).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Fernanda Alves Oliveira"))
                .andExpect(jsonPath("$.papeis[?(@=='Responsável')]").exists());
        mvc.perform(get("/membros/99999")).andExpect(status().isNotFound());
    }

    @Test
    void alterarMantemOProprioCpf() throws Exception {
        int memCod = cadastrar("Bia Alterada", CPF_A);
        mvc.perform(put("/membros/" + memCod).contentType(MediaType.APPLICATION_JSON)
                .content(pessoa("Bia Alterada", CPF_A, "18988887777")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value("18988887777"));
        assertEquals("18988887777", jdbc.queryForObject("SELECT mem_tel FROM membro WHERE mem_cod = ?", String.class, memCod));
    }

    @Test
    void alterarComCpfDeOutraPessoaRecusa() throws Exception {
        cadastrar("Carla Primeira", CPF_A);
        int segunda = cadastrar("Duda Segunda", CPF_B);
        mvc.perform(put("/membros/" + segunda).contentType(MediaType.APPLICATION_JSON)
                .content(pessoa("Duda Segunda", CPF_A, "18999990000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Já existe outro membro com esse CPF"));
    }

    @Test
    void excluirEhLogico() throws Exception {
        int memCod = cadastrar("Edu Excluido", CPF_A);
        mvc.perform(delete("/membros/" + memCod)).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").exists());
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM membro WHERE mem_cod = ? AND mem_dt_fim = CURRENT_DATE", Integer.class, memCod));
        List<Integer> codigos = JsonPath.read(listar("/membros"), "$[*].mem_cod");
        assertFalse(codigos.contains(memCod));
    }

    @Test
    void cadastroInvalidoDevolve400ComMensagem() throws Exception {
        mvc.perform(post("/membros").contentType(MediaType.APPLICATION_JSON)
                .content(pessoa("Ana 1", CPF_A, "18999990000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Nome não pode conter números"));
    }

    @Test
    void cadastroComCpfRepetidoDevolve400() throws Exception {
        cadastrar("Fabi Primeira", CPF_A);
        mvc.perform(post("/membros").contentType(MediaType.APPLICATION_JSON)
                .content(pessoa("Fabi Segunda", CPF_A, "18999990000")))
                .andExpect(status().isBadRequest());
    }
}
