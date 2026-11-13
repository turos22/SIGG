package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

// Ajustes de schema do bloco de gestão: patrulha sem líder, frequência sem atividade especial
// e um único registro por escoteiro e data.
@SpringBootTest
@Transactional
class SchemaGestaoTest {

    private static final String INSERT_FREQUENCIA = "INSERT INTO frequencia (freq_cod, freq_data, freq_status,"
            + " escoteiro_esc_cod, atividade_especial_atv_cod)"
            + " VALUES ((SELECT COALESCE(MAX(freq_cod), 0) + 1 FROM frequencia), DATE '2026-11-07', 'P', 2007, NULL)";

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void patrulhaAceitaLiderNulo() {
        assertDoesNotThrow(() -> jdbc.update("INSERT INTO patrulha (pat_cod, pat_nome, pat_lider_cod,"
                + " pat_sublider_cod, tropa_tro_cod) VALUES (99, 'Teste', NULL, NULL, 1)"));
    }

    @Test
    void frequenciaAceitaSemAtividade() {
        assertDoesNotThrow(() -> jdbc.update(INSERT_FREQUENCIA));
    }

    @Test
    void frequenciaRecusaEscoteiroDuasVezesNoDia() {
        jdbc.update(INSERT_FREQUENCIA);
        assertThrows(DuplicateKeyException.class, () -> jdbc.update(INSERT_FREQUENCIA));
    }
}
