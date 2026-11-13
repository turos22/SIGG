package com.example.sigg_backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

// A rota existe e passa pelo tratador de erros: sem usuário logado (segurança desligada nos testes) devolve 401
@SpringBootTest
@AutoConfigureMockMvc
class RotaMinhaTropaTest {

    @Autowired
    MockMvc mvc;

    @Test
    void semUsuarioLogadoDevolve401() throws Exception {
        mvc.perform(get("/frequencias/minha-tropa")).andExpect(status().isUnauthorized());
    }
}
