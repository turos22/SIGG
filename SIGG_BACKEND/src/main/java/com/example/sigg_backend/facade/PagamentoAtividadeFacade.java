package com.example.sigg_backend.facade;

import com.example.sigg_backend.db.dao.PagamentoMensalidadeDAO;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.FormaPagamento;
import com.example.sigg_backend.models.PagamentoAtividade;
import com.example.sigg_backend.models.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PagamentoAtividadeFacade {

    private final AutorizacaoFacade autorizacao;

    public PagamentoAtividadeFacade(AutorizacaoFacade autorizacao) {
        this.autorizacao = autorizacao;
    }

    public FormaPagamento[] listarFormasPagamento() {
        autorizacao.exigirDiretoria();
        return new PagamentoMensalidadeDAO().listarFormasPagamento();
    }

    public Escoteiro[] pesquisarEscoteiros(String busca) {
        autorizacao.exigirDiretoria();
        if (busca == null || busca.isBlank()) {
            return new PagamentoMensalidadeDAO().listarEscoteirosComResumo();
        }
        String termo = busca.trim();
        if (termo.matches("[\\d.\\-]+")) {
            termo = termo.replaceAll("\\D", "");
        }
        List<Escoteiro> lista = new Escoteiro().pesquisar(termo);
        if (lista.isEmpty()) {
            throw new NaoEncontradoException("Escoteiro não localizado no sistema.");
        }
        return lista.toArray(new Escoteiro[0]);
    }

    public Map<String, Object> consultarAtividadesEscoteiro(int escCod) {
        autorizacao.exigirDiretoria();
        Escoteiro escoteiro = new Escoteiro().buscarPorCodigo(escCod);
        if (escoteiro == null) {
            throw new NaoEncontradoException("Escoteiro não encontrado.");
        }

        boolean possuiMensalidadeVencida = new PagamentoMensalidadeDAO().possuiMensalidadeVencidaEmAberto(escCod);
        boolean inadimplente = escoteiro.getEsc_sit_fin() == 1 || possuiMensalidadeVencida;
        PagamentoAtividade[] atividades = PagamentoAtividade.listarPorEscoteiro(escCod);

        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("esc_cod", escoteiro.getEsc_cod());
        resumo.put("mem_cod", escoteiro.getMembro().getMem_cod());
        resumo.put("nome", escoteiro.getMembro().getNome());
        resumo.put("cpf", escoteiro.getMembro().getCPF());
        resumo.put("patrulha", escoteiro.getPatrulha() != null ? escoteiro.getPatrulha().getNome() : "—");
        resumo.put("inadimplente", inadimplente);
        resumo.put("atividades", atividades);
        return resumo;
    }

    @Transactional
    public Map<String, Object> registrarPagamento(int crCod, BigDecimal valorRecebido, int formaPagamentoCod,
                                                  LocalDate dataPagamento, boolean confirmarDuplicado) {
        autorizacao.exigirPerfil(List.of(UsuarioLogado.ADMIN, UsuarioLogado.FINANCEIRO));

        if (valorRecebido == null || valorRecebido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor recebido deve ser maior que zero.");
        }

        PagamentoAtividade pag = PagamentoAtividade.buscarPorConta(crCod);
        if (pag == null) {
            throw new NaoEncontradoException("Inscrição em atividade paga não encontrada.");
        }

        boolean possuiVencida = new PagamentoMensalidadeDAO().possuiMensalidadeVencidaEmAberto(pag.getEscCod());
        if (pag.isInadimplente() || possuiVencida) {
            throw new RegraNegocioException(
                    "Este escoteiro está inadimplente. Não é permitido registrar pagamento de atividades pagas enquanto houver mensalidades pendentes. Regularize a situação financeira primeiro."
            );
        }

        BigDecimal saldoDevido = pag.saldoRestante();
        if (saldoDevido.compareTo(BigDecimal.ZERO) <= 0 && !confirmarDuplicado) {
            throw new RegraNegocioException(
                    "Este escoteiro já realizou o pagamento para esta atividade. Confirme caso deseje registrar um pagamento adicional."
            );
        }

        BigDecimal valorFinal = valorRecebido.setScale(2, RoundingMode.HALF_UP);
        BigDecimal diferenca = pag.verificarDivergencia(valorFinal);
        LocalDate dtPagto = dataPagamento != null ? dataPagamento : LocalDate.now();

        pag.setValorRecebido(valorFinal);
        pag.setFormaPagamentoCod(formaPagamentoCod);
        pag.setDataPagamento(dtPagto);

        String obsDivergencia = "";
        if (saldoDevido.compareTo(BigDecimal.ZERO) <= 0 && confirmarDuplicado) {
            obsDivergencia = "Pagamento adicional/complementar confirmado pelo financeiro.";
        } else if (diferenca.compareTo(BigDecimal.ZERO) < 0) {
            obsDivergencia = "Pagamento parcial da atividade. Saldo restante de R$ " + diferenca.abs() + " mantido em aberto.";
        } else if (diferenca.compareTo(BigDecimal.ZERO) > 0) {
            obsDivergencia = "Valor recebido superior ao valor da atividade (excedente de R$ " + diferenca + ").";
        }
        pag.setObservacao(obsDivergencia);

        if (!pag.registrar()) {
            throw new IllegalStateException("Não foi possível registrar o pagamento da atividade.");
        }

        Map<String, Object> recibo = new LinkedHashMap<>();
        recibo.put("numero", "REC-ATIV-" + crCod + "-" + (System.currentTimeMillis() % 10000));
        recibo.put("escoteiro", pag.getEscoteiroNome());
        recibo.put("cpf", pag.getEscoteiroCpf());
        recibo.put("matricula", pag.getEscCod());
        recibo.put("atividade", pag.getAtividadeNome());
        recibo.put("dataAtividade", pag.getAtividadeData() != null ? pag.getAtividadeData().toString() : "—");
        recibo.put("valorAtividade", pag.getValorAtividade());
        recibo.put("valorPago", valorFinal);
        recibo.put("observacao", obsDivergencia);
        recibo.put("dataPagamento", dtPagto.toString());

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", "Pagamento da atividade \"" + pag.getAtividadeNome() + "\" registrado com sucesso!");
        resposta.put("recibo", recibo);
        return resposta;
    }

    @Transactional
    public Map<String, Object> estornarPagamento(int crCod) {
        autorizacao.exigirPerfil(List.of(UsuarioLogado.ADMIN, UsuarioLogado.FINANCEIRO));

        PagamentoAtividade pag = PagamentoAtividade.buscarPorConta(crCod);
        if (pag == null) {
            throw new NaoEncontradoException("Atividade não encontrada.");
        }
        if (pag.getValorPago().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Esta atividade não possui pagamento registrado para estorno.");
        }

        if (!pag.estornar()) {
            throw new IllegalStateException("Não foi possível estornar o pagamento da atividade.");
        }

        return Map.of("mensagem", "Estorno concluído! A participação na atividade \"" + pag.getAtividadeNome() + "\" foi reaberta.");
    }
}