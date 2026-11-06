package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.sigg_backend.models.Conta;
import com.example.sigg_backend.models.Cpf;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.TokenConta;
import com.example.sigg_backend.models.UsuarioLogado;

class AutenticacaoModelosTest {

    @Test
    void cpfValidaDigitosVerificadores() {
        assertTrue(Cpf.valido("56871710370"));
        assertTrue(Cpf.valido("568.717.103-70"));
        assertFalse(Cpf.valido("56871710371"));
        assertFalse(Cpf.valido("11111111111"));
        assertFalse(Cpf.valido("123"));
        assertFalse(Cpf.valido(null));
    }

    @Test
    void politicaDeSenhaExigeTamanhoLetraENumero() {
        assertEquals("A senha deve ter no mínimo 8 caracteres", Conta.erroDeSenha("abc12"));
        assertEquals("A senha deve conter ao menos um número", Conta.erroDeSenha("abcdefgh"));
        assertEquals("A senha deve conter ao menos uma letra", Conta.erroDeSenha("12345678"));
        assertEquals("A senha deve ter no máximo 72 bytes", Conta.erroDeSenha("a1".repeat(37)));
        assertEquals(null, Conta.erroDeSenha("Guaypore@2026"));
    }

    @Test
    void senhaDefinidaEhGuardadaComHashEConfere() {
        Conta conta = new Conta();
        conta.definirSenha("Guaypore@2026");
        assertNotEquals("Guaypore@2026", conta.hashDaSenha());
        assertTrue(conta.senhaConfere("Guaypore@2026"));
        assertFalse(conta.senhaConfere("outraSenha1"));
    }

    @Test
    void contaBloqueiaAposCincoFalhasEDesbloqueiaDepoisDoPrazo() {
        Conta conta = new Conta();
        LocalDateTime agora = LocalDateTime.of(2026, 11, 6, 10, 0);
        for (int i = 0; i < Conta.MAX_TENTATIVAS - 1; i++) {
            conta.registrarFalha(agora);
            assertFalse(conta.estaBloqueada(agora));
        }
        conta.registrarFalha(agora);
        assertTrue(conta.estaBloqueada(agora.plusMinutes(Conta.MINUTOS_DE_BLOQUEIO - 1)));
        assertFalse(conta.estaBloqueada(agora.plusMinutes(Conta.MINUTOS_DE_BLOQUEIO)));
        conta.zerarFalhas();
        assertFalse(conta.temFalhasRegistradas());
    }

    @Test
    void somenteMaioresDeIdadePassamNaVerificacao() {
        Conta conta = new Conta();
        Membro membro = new Membro();
        conta.definirMembro(membro);
        LocalDate hoje = LocalDate.of(2026, 11, 6);
        membro.setDt_nascto(LocalDate.of(2008, 11, 6));
        assertTrue(conta.maiorDeIdade(hoje));
        membro.setDt_nascto(LocalDate.of(2008, 11, 7));
        assertFalse(conta.maiorDeIdade(hoje));
    }

    @Test
    void tokenGuardaApenasOHashEExpira() {
        String puro = TokenConta.gerarTokenPuro();
        assertNotEquals(puro, TokenConta.gerarTokenPuro());
        assertEquals(64, TokenConta.hashDe(puro).length());
        assertEquals(TokenConta.hashDe(puro), TokenConta.hashDe(puro));

        TokenConta token = new TokenConta();
        LocalDateTime agora = LocalDateTime.of(2026, 11, 6, 10, 0);
        token.setExpira(agora.plusMinutes(30));
        assertTrue(token.estaValido(agora));
        assertFalse(token.estaValido(agora.plusMinutes(30)));
        token.setUsado_em(agora);
        assertFalse(token.estaValido(agora));
    }

    @Test
    void perfisDefinemSeOUsuarioEhDiretoria() {
        assertTrue(new UsuarioLogado(1, "A", List.of(UsuarioLogado.FINANCEIRO)).ehDiretoria());
        assertTrue(new UsuarioLogado(1, "A", List.of(UsuarioLogado.RESPONSAVEL, UsuarioLogado.CHEFE)).ehDiretoria());
        assertFalse(new UsuarioLogado(2, "B", List.of(UsuarioLogado.RESPONSAVEL)).ehDiretoria());
        assertFalse(new UsuarioLogado(3, "C", List.of(UsuarioLogado.ESCOTEIRO)).ehDiretoria());
    }
}
