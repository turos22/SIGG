package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Uf;

// Regras de campo do membro: letras onde é texto, números onde é número.
class ValidacaoMembroTest {

    private static Membro valido() {
        Membro membro = new Membro();
        membro.setNome("Joao da Silva");
        membro.setDt_nascto(LocalDate.of(1990, 5, 20));
        membro.setCPF("52998224725");
        membro.setRG("123456789");
        membro.setCEP("19025080");
        membro.setEndereco("Rua 7 de Setembro");
        membro.setEndereco_num(140);
        membro.setEndereco_bairro("Jardim Paulista");
        membro.setCidade("Presidente Prudente");
        membro.setEstado("SP");
        membro.setTelefone("18999990000");
        membro.setEmail("joao@example.com");
        return membro;
    }

    private static String problemaCom(Consumer<Membro> alteracao) {
        Membro membro = valido();
        alteracao.accept(membro);
        return membro.validar();
    }

    @Test
    void membroValidoNaoTemProblema() {
        assertNull(valido().validar());
    }

    @Test
    void nomeComNumeroEhRecusado() {
        assertEquals("Nome não pode conter números", problemaCom(m -> m.setNome("João 2")));
    }

    @Test
    void rgRecusaPontuacaoEAceitaDigitoX() {
        assertEquals("RG aceita só números e a letra X", problemaCom(m -> m.setRG("12.345")));
        assertNull(problemaCom(m -> m.setRG("1234567X")));
    }

    @Test
    void cidadeEBairroComNumeroSaoRecusados() {
        assertEquals("Cidade não pode conter números", problemaCom(m -> m.setCidade("Pres. Prudente 1")));
        assertEquals("Bairro não pode conter números", problemaCom(m -> m.setEndereco_bairro("Vila 3")));
    }

    @Test
    void estadoPrecisaSerSiglaDeUf() {
        assertEquals("Estado inválido", problemaCom(m -> m.setEstado("São Paulo")));
        assertEquals("Estado inválido", problemaCom(m -> m.setEstado("sp")));
        assertNull(problemaCom(m -> m.setEstado("MG")));
    }

    @Test
    void cpfPrecisaTerDigitosVerificadoresCorretos() {
        assertEquals("CPF inválido", problemaCom(m -> m.setCPF("11111111111")));
        assertEquals("CPF inválido", problemaCom(m -> m.setCPF("52998224724")));
        assertEquals("CPF deve ter 11 dígitos, só números", problemaCom(m -> m.setCPF("529.982.247-25")));
    }

    @Test
    void enderecoAceitaLetrasENumeros() {
        assertNull(problemaCom(m -> m.setEndereco("Rua 7 de Setembro, 2º andar")));
    }

    @Test
    void ufTemAs27SiglasEValidaSoMaiusculas() {
        assertEquals(27, Uf.SIGLAS.size());
        assertTrue(Uf.valida("SP"));
        assertFalse(Uf.valida("sp"));
        assertFalse(Uf.valida("XX"));
        assertFalse(Uf.valida(null));
    }
}
