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

// Chefe e presidente não são cadastro novo: são promoção de quem já é escoteiro.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PromocaoTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private int contar(String sql, Object... valores) {
        return jdbc.queryForObject(sql, Integer.class, valores);
    }

    private ResultActions enviar(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void promoverEscoteiroAChefeGravaSoOChefe() throws Exception {
        int chefeCod = jdbc.queryForObject("SELECT max(chefe_cod) FROM chefe", Integer.class) + 1;
        int escCodDoPedro = jdbc.queryForObject("SELECT esc_cod FROM escoteiro WHERE mem_cod = 11", Integer.class);
        int membrosAntes = contar("SELECT count(*) FROM membro");
        int escoteirosAntes = contar("SELECT count(*) FROM escoteiro");

        enviar("/escoteiros/11/chefe", "{\"gestao_ini\":\"2026-11-01\",\"tropa\":{\"cod\":2}}") // Pedro Silva Ramos
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chefe_cod").value(chefeCod))
                .andExpect(jsonPath("$.esc_cod").value(escCodDoPedro)) // herdado do Escoteiro
                .andExpect(jsonPath("$.membro.nome").value("Pedro Silva Ramos"));

        Map<String, Object> chefe = jdbc.queryForMap(
                "SELECT chefe_cod, chefe_gestao_ini::text AS ini, chefe_gestao_fim, tropa_tro_cod FROM chefe WHERE mem_cod = 11");
        assertEquals(chefeCod, chefe.get("chefe_cod"));
        assertEquals("2026-11-01", chefe.get("ini"));
        assertEquals(null, chefe.get("chefe_gestao_fim"));
        assertEquals(2, chefe.get("tropa_tro_cod"));
        assertEquals(membrosAntes, contar("SELECT count(*) FROM membro"));
        assertEquals(escoteirosAntes, contar("SELECT count(*) FROM escoteiro"));
    }

    @Test
    void promoverEscoteiroAPresidente() throws Exception {
        int presCod = jdbc.queryForObject("SELECT max(pres_cod) FROM presidente", Integer.class) + 1;

        enviar("/escoteiros/12/presidente", "{\"gestao_ini\":\"2026-01-01\",\"gestao_fim\":\"2027-12-31\"}") // Laura Silva Ramos
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pres_cod").value(presCod))
                .andExpect(jsonPath("$.membro.nome").value("Laura Silva Ramos"));

        assertEquals(1, contar("SELECT count(*) FROM presidente WHERE mem_cod = 12 AND pres_cod = ? "
                + "AND pres_gestao_ini = DATE '2026-01-01' AND pres_gestao_fim = DATE '2027-12-31'", presCod));
    }
}
