package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

// SEM @Transactional no teste: aqui o que vale é a transação da própria Façade.
// O membro é gravado antes do escoteiro; se o escoteiro falhar, o membro tem de ser desfeito junto.
@SpringBootTest
@AutoConfigureMockMvc
class AtomicidadeDoCadastroTest {

    private static final String CPF = "11144477735";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    // Se o código estiver errado e deixar lixo, o banco é limpo mesmo assim
    @AfterEach
    void limpar() {
        jdbc.update("DELETE FROM escoteiro WHERE mem_cod IN (SELECT mem_cod FROM membro WHERE mem_cpf = ?)", CPF);
        jdbc.update("DELETE FROM membro WHERE mem_cpf = ?", CPF);
    }

    private static String escoteiro(int patrulha, String extra) {
        return """
                {"membro":{"nome":"Falha de Banco","dt_nascto":"2013-01-01","CPF":"%s","RG":"987654321",
                 "CEP":"19000000","endereco":"Rua Teste","endereco_num":20,"endereco_bairro":"Centro",
                 "cidade":"Presidente Prudente","estado":"SP"},"cat_mensalidade":1,"patrulha":{"cod":%d}%s}""".formatted(CPF, patrulha, extra);
    }

    // O Anjo inexistente só quebra na FK do banco, depois do membro já ter sido inserido
    @Test
    void escoteiroComFalhaDoBancoNaoDeixaMembroGravado() {
        String json = escoteiro(3, ",\"anjo\":99999");

        // Falha do banco não é regra de negócio: sobe como erro 500
        assertThrows(Exception.class,
                () -> mvc.perform(post("/escoteiros").contentType(MediaType.APPLICATION_JSON).content(json)));

        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM membro WHERE mem_cpf = ?", Integer.class, CPF));
    }

    // Patrulha inexistente é validada antes de gravar qualquer coisa: 404 e nada no banco
    @Test
    void escoteiroComPatrulhaInexistenteDevolve404SemGravarMembro() throws Exception {
        mvc.perform(post("/escoteiros").contentType(MediaType.APPLICATION_JSON).content(escoteiro(99, "")))
                .andExpect(status().isNotFound());

        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM membro WHERE mem_cpf = ?", Integer.class, CPF));
    }
}
