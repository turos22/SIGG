package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

// RF_F5 Ofertar Novas Atividades Pagas: fluxo básico e fluxos alternativos 6.1 e 6.3.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OfertaDeAtividadePagaTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;

    private final LocalDate daquiUmMes = LocalDate.now().plusMonths(1);
    private final LocalDate daquiDuasSemanas = LocalDate.now().plusWeeks(2);

    private String atividade(String nome, LocalDate data, LocalDate prazo) {
        return """
                {"nome":"%s","descricao":"Fim de semana no sítio","data":"%s","local":"Sítio Boa Vista",
                 "valor":150.50,"vagas":30,"prazoInteresse":"%s"}""".formatted(nome, data, prazo);
    }

    private ResultActions ofertar(String json) throws Exception {
        return mvc.perform(post("/atividades-pagas").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private int contar(String sql, Object... valores) {
        return jdbc.queryForObject(sql, Integer.class, valores);
    }

    @Test
    void ofertarGravaAAtividadeEElaApareceNaListaDeAbertas() throws Exception {
        int proximo = jdbc.queryForObject("SELECT max(atv_cod) FROM atividade_especial", Integer.class) + 1;

        ofertar(atividade("Acampamento de Verão", daquiUmMes, daquiDuasSemanas))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(proximo))
                .andExpect(jsonPath("$.vagas").value(30));

        assertEquals(1, contar("SELECT count(*) FROM atividade_especial WHERE atv_cod = ? AND atv_valor = 150.5"
                + " AND atv_local = 'Sítio Boa Vista' AND atv_vagas = 30 AND atv_prazo_interesse = ?",
                proximo, daquiDuasSemanas));

        mvc.perform(get("/atividades-pagas?abertas=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", Matchers.hasItem("Acampamento de Verão")))
                .andExpect(jsonPath("$[*].nome", Matchers.not(Matchers.hasItem("Passeio ao Zoológico"))));
    }

    @Test
    void listarTodasNaoTrazReunioesNemORegistroTecnicoDeMensalidade() throws Exception {
        mvc.perform(get("/atividades-pagas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome", Matchers.hasItem("Passeio ao Zoológico")))
                .andExpect(jsonPath("$[*].nome", Matchers.not(Matchers.hasItem("Reunião Semanal 01/08"))))
                .andExpect(jsonPath("$[*].codigo", Matchers.not(Matchers.hasItem(1))));
    }

    @Test
    void camposObrigatoriosFaltandoSaoListadosENadaEGravado() throws Exception {
        int antes = contar("SELECT count(*) FROM atividade_especial");
        ofertar("{\"nome\":\"  \",\"valor\":10}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Campos obrigatórios não preenchidos: nome, data de realização,"
                        + " local, número de vagas, prazo para manifestação de interesse"));
        assertEquals(antes, contar("SELECT count(*) FROM atividade_especial"));
    }

    @Test
    void mesmaAtividadeComMesmoNomeEDataERecusada() throws Exception {
        ofertar(atividade("Trilha Noturna", daquiUmMes, daquiDuasSemanas)).andExpect(status().isCreated());
        ofertar(atividade("trilha noturna", daquiUmMes, daquiDuasSemanas))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro")
                        .value("Já existe uma atividade paga cadastrada com o mesmo nome e data de realização"));
        // Mesmo nome em outra data é outra atividade
        ofertar(atividade("Trilha Noturna", daquiUmMes.plusDays(7), daquiDuasSemanas)).andExpect(status().isCreated());
    }

    @Test
    void datasInvalidasSaoRecusadas() throws Exception {
        ofertar(atividade("No passado", LocalDate.now().minusDays(1), LocalDate.now().minusDays(2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A data de realização não pode estar no passado"));
        ofertar(atividade("Prazo depois", daquiDuasSemanas, daquiUmMes))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro")
                        .value("O prazo para manifestação de interesse não pode ser depois da data da atividade"));
    }

    @Test
    void valorEVagasPrecisamSerPositivos() throws Exception {
        ofertar(atividade("Grátis", daquiUmMes, daquiDuasSemanas).replace("150.50", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O valor da atividade paga deve ser maior que zero"));
        ofertar(atividade("Sem vagas", daquiUmMes, daquiDuasSemanas).replace("\"vagas\":30", "\"vagas\":0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O número de vagas deve ser maior que zero"));
    }
}
