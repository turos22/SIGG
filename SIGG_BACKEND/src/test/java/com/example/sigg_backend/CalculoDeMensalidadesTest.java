package com.example.sigg_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Mensalidade;
import com.example.sigg_backend.models.SaldoAdotado;
import com.example.sigg_backend.models.SaldoMensalidades;

class CalculoDeMensalidadesTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 22);

    private Mensalidade mensalidade(int mes, String valor, String pago, LocalDate vencimento) {
        Mensalidade mensalidade = new Mensalidade();
        mensalidade.setMens_mes(mes);
        mensalidade.setMens_ano(2026);
        mensalidade.setMens_valor(new BigDecimal(valor));
        mensalidade.setValor_pago(new BigDecimal(pago));
        mensalidade.setDt_vencimento(vencimento);
        return mensalidade;
    }

    private Escoteiro escoteiro(int memCod, int escCod, String nome) {
        Membro membro = new Membro();
        membro.setMem_cod(memCod);
        membro.setNome(nome);
        membro.setCPF("00000000000");
        Escoteiro escoteiro = new Escoteiro();
        escoteiro.setMembro(membro);
        escoteiro.setEsc_cod(escCod);
        escoteiro.setAnjo(escCod);
        return escoteiro;
    }

    @Test
    void situacaoDaMensalidadeDependeDoPagamentoEDoVencimento() {
        assertEquals(Mensalidade.PAGA, mensalidade(9, "80.00", "80.00", HOJE.minusDays(20)).situacao(HOJE));
        assertEquals(Mensalidade.ATRASADA, mensalidade(9, "70.00", "40.00", HOJE.minusDays(12)).situacao(HOJE));
        assertEquals(Mensalidade.PARCIAL, mensalidade(10, "70.00", "40.00", HOJE.plusDays(3)).situacao(HOJE));
        assertEquals(Mensalidade.ABERTA, mensalidade(10, "70.00", "0.00", HOJE.plusDays(3)).situacao(HOJE));
    }

    @Test
    void saldoDaMensalidadeNuncaFicaNegativo() {
        assertEquals(new BigDecimal("0.00"), mensalidade(10, "80.00", "90.00", HOJE).saldoReal());
        assertEquals(new BigDecimal("30.00"), mensalidade(10, "70.00", "40.00", HOJE).saldoReal());
    }

    @Test
    void escoteiroComumSomaSomenteAsMensalidadesEmAberto() {
        SaldoMensalidades saldo = new SaldoMensalidades();
        saldo.identificar(escoteiro(30, 2012, "Davi"));
        saldo.calcular(List.of(
                mensalidade(8, "90.00", "90.00", HOJE.minusDays(60)),
                mensalidade(9, "90.00", "40.00", HOJE.minusDays(12)),
                mensalidade(10, "90.00", "0.00", HOJE.minusDays(12))), List.of());

        assertEquals(1, saldo.getPagas().size());
        assertEquals(2, saldo.getAbertas().size());
        assertEquals(new BigDecimal("140.00"), saldo.getTotal_a_pagar());
        assertFalse(saldo.isAnjo());
        assertFalse(saldo.isAdotado());
    }

    @Test
    void adotadoFicaIsentoEVeTodasAsMensalidadesComoPagas() {
        SaldoMensalidades saldo = new SaldoMensalidades();
        saldo.identificar(escoteiro(24, 2015, "Rafael"));
        saldo.apadrinhar("Henrique");
        saldo.calcular(List.of(
                mensalidade(9, "90.00", "90.00", HOJE.minusDays(40)),
                mensalidade(10, "90.00", "0.00", HOJE.minusDays(12))), List.of());

        assertTrue(saldo.isAdotado());
        assertTrue(saldo.isIsento());
        assertEquals(0, saldo.getAbertas().size());
        assertEquals(2, saldo.getPagas().size());
        assertEquals(new BigDecimal("0.00"), saldo.getTotal_a_pagar());
        assertEquals(Mensalidade.PAGA, saldo.getPagas().get(1).getSituacao());
    }

    @Test
    void anjoConsolidaAsMensalidadesPropriasEAsDosAdotados() {
        SaldoAdotado rafael = new SaldoAdotado();
        rafael.setNome("Rafael");
        rafael.calcular(List.of(mensalidade(10, "90.00", "0.00", HOJE.minusDays(12))));
        SaldoAdotado sofia = new SaldoAdotado();
        sofia.setNome("Sofia");
        sofia.calcular(List.of(
                mensalidade(9, "90.00", "90.00", HOJE.minusDays(40)),
                mensalidade(10, "90.00", "30.00", HOJE.minusDays(12))));

        SaldoMensalidades saldo = new SaldoMensalidades();
        saldo.identificar(escoteiro(8, 2006, "Henrique"));
        saldo.calcular(List.of(mensalidade(10, "0.01", "0.00", HOJE)), List.of(rafael, sofia));

        assertTrue(saldo.isAnjo());
        assertEquals(new BigDecimal("150.00"), saldo.getTotal_adotados());
        assertEquals(new BigDecimal("0.01"), saldo.getTotal_proprio());
        assertEquals(new BigDecimal("150.01"), saldo.getTotal_a_pagar());
    }
}
