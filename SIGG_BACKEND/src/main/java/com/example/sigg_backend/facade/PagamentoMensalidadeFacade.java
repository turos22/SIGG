package com.example.sigg_backend.facade;

import com.example.sigg_backend.models.CategoriaMensalidade;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.FormaPagamento;
import com.example.sigg_backend.models.PagamentoMensalidade;
import com.example.sigg_backend.models.SituacaoEscoteiro;
import com.example.sigg_backend.models.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.example.sigg_backend.db.util.Conexao;

@Service
public class PagamentoMensalidadeFacade {

    private final AutorizacaoFacade autorizacao;

    public PagamentoMensalidadeFacade(AutorizacaoFacade autorizacao) {
        this.autorizacao = autorizacao;
    }

    public FormaPagamento[] listarFormasPagamento(Conexao conexao) {
        autorizacao.exigirDiretoria(conexao);
        return PagamentoMensalidade.listarFormasPagamento(conexao);
    }

    public Escoteiro[] pesquisarEscoteiros(Conexao conexao, String busca) {
        autorizacao.exigirDiretoria(conexao);
        if (busca == null || busca.isBlank()) {
            return PagamentoMensalidade.listarEscoteirosComResumo(conexao);
        }
        String termo = busca.trim();
        if (termo.matches("[\\d.\\-]+")) {
            termo = termo.replaceAll("\\D", "");
        }
        List<Escoteiro> lista = new Escoteiro().pesquisar(conexao, termo);
        if (lista.isEmpty()) {
            throw new NaoEncontradoException("Escoteiro não localizado no sistema.");
        }
        return lista.toArray(new Escoteiro[0]);
    }

    // Grade de situação financeira: em dia, pendente (calculado) ou inadimplente (marcado pelo financeiro)
    public List<SituacaoEscoteiro> listarSituacoes(Conexao conexao, Integer troCod) {
        autorizacao.exigirDiretoria(conexao);
        return PagamentoMensalidade.listarSituacoes(conexao, troCod);
    }

    // Só o financeiro marca, e só quem está pendente: o sistema nunca marca inadimplência sozinho
    @Transactional
    public Map<String, String> marcarInadimplente(Conexao conexao, int escCod) {
        autorizacao.exigirPerfil(conexao, List.of(UsuarioLogado.ADMIN, UsuarioLogado.FINANCEIRO));
        Escoteiro escoteiro = new Escoteiro().buscarPorCodigo(conexao, escCod);
        if (escoteiro == null) {
            throw new NaoEncontradoException("Escoteiro não encontrado.");
        }
        if (escoteiro.getEsc_sit_fin() == 1) {
            throw new RegraNegocioException("Este escoteiro já está inadimplente");
        }
        if (!PagamentoMensalidade.possuiMensalidadeVencidaEmAberto(conexao, escCod)) {
            throw new RegraNegocioException("Este escoteiro não possui mensalidades em atraso e não pode ser marcado como Inadimplente");
        }
        if (!PagamentoMensalidade.atualizarSituacaoFinanceiraEscoteiro(conexao, escCod, 1)) {
            throw new IllegalStateException("Não foi possível marcar o escoteiro como inadimplente.");
        }
        return Map.of("mensagem", escoteiro.getMembro().getNome() + " foi marcado(a) como inadimplente.");
    }

    public Map<String, Object> consultarSituacaoEscoteiro(Conexao conexao, int escCod) {
        autorizacao.exigirDiretoria(conexao);
        Escoteiro escoteiro = new Escoteiro().buscarPorCodigo(conexao, escCod);
        if (escoteiro == null) {
            throw new NaoEncontradoException("Escoteiro não encontrado.");
        }

        CategoriaMensalidade categoria = CategoriaMensalidade.buscarPorId(conexao, escoteiro.getCat_mensalidade());
        PagamentoMensalidade[] mensalidades = PagamentoMensalidade.listarPorEscoteiro(conexao, escCod);

        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("esc_cod", escoteiro.getEsc_cod());
        resumo.put("mem_cod", escoteiro.getMembro().getMem_cod());
        resumo.put("nome", escoteiro.getMembro().getNome());
        resumo.put("cpf", escoteiro.getMembro().getCPF());
        resumo.put("patrulha", escoteiro.getPatrulha() != null ? escoteiro.getPatrulha().getNome() : "—");
        resumo.put("categoria", categoria != null ? categoria.getDescricao() : "Categoria " + escoteiro.getCat_mensalidade());
        resumo.put("valorCategoria", categoria != null ? categoria.getValor() : BigDecimal.ZERO);
        resumo.put("inadimplente", escoteiro.getEsc_sit_fin() == 1);
        resumo.put("mensalidades", mensalidades);
        return resumo;
    }

    @Transactional
    public Map<String, Object> registrarPagamento(Conexao conexao, int crCod, BigDecimal valorRecebido, int formaPagamentoCod, LocalDate dataPagamento) {
        autorizacao.exigirPerfil(conexao, List.of(UsuarioLogado.ADMIN, UsuarioLogado.FINANCEIRO));

        if (valorRecebido == null || valorRecebido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor recebido deve ser maior que zero.");
        }

        PagamentoMensalidade pag = PagamentoMensalidade.buscarPorConta(conexao, crCod);
        if (pag == null) {
            throw new NaoEncontradoException("Mensalidade não encontrada.");
        }

        BigDecimal saldoDevido = pag.saldoRestante();
        if (saldoDevido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Esta mensalidade já se encontra totalmente quitada.");
        }

        BigDecimal valorFinal = valorRecebido.setScale(2, RoundingMode.HALF_UP);
        BigDecimal diferenca = pag.verificarDivergencia(valorFinal);
        LocalDate dtPagto = dataPagamento != null ? dataPagamento : LocalDate.now();

        pag.setValorRecebido(valorFinal);
        pag.setFormaPagamentoCod(formaPagamentoCod);
        pag.setDataPagamento(dtPagto);

        String obsDivergencia = "";
        if (diferenca.compareTo(BigDecimal.ZERO) < 0) {
            obsDivergencia = "Pagamento parcial. Diferença de R$ " + diferenca.abs() + " mantida em aberto.";
        } else if (diferenca.compareTo(BigDecimal.ZERO) > 0) {
            obsDivergencia = "Pagamento superior ao devido. Excedente de R$ " + diferenca + " registrado como crédito.";
        }
        pag.setObservacao(obsDivergencia);

        if (!pag.registrar(conexao)) {
            throw new IllegalStateException("Não foi possível registrar o pagamento da mensalidade.");
        }

        boolean regularizado = false;
        if (pag.isInadimplente() && !PagamentoMensalidade.possuiMensalidadeVencidaEmAberto(conexao, pag.getEscCod())) {
            PagamentoMensalidade.atualizarSituacaoFinanceiraEscoteiro(conexao, pag.getEscCod(), 0);
            regularizado = true;
        }

        Map<String, Object> recibo = new LinkedHashMap<>();
        recibo.put("numero", "REC-MENS-" + crCod + "-" + (System.currentTimeMillis() % 10000));
        recibo.put("escoteiro", pag.getEscoteiroNome());
        recibo.put("cpf", pag.getEscoteiroCpf());
        recibo.put("matricula", pag.getEscCod());
        recibo.put("referencia", pag.getReferencia());
        recibo.put("valorDevido", saldoDevido);
        recibo.put("valorPago", valorFinal);
        recibo.put("diferenca", diferenca);
        recibo.put("observacao", obsDivergencia);
        recibo.put("dataPagamento", dtPagto.toString());
        recibo.put("regularizado", regularizado);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("mensagem", regularizado
                ? "Pagamento registrado! A situação financeira do escoteiro foi regularizada automaticamente."
                : "Pagamento de mensalidade registrado com sucesso!");
        resposta.put("recibo", recibo);
        return resposta;
    }

    @Transactional
    public Map<String, Object> estornarPagamento(Conexao conexao, int crCod) {
        autorizacao.exigirPerfil(conexao, List.of(UsuarioLogado.ADMIN, UsuarioLogado.FINANCEIRO));

        PagamentoMensalidade pag = PagamentoMensalidade.buscarPorConta(conexao, crCod);
        if (pag == null) {
            throw new NaoEncontradoException("Mensalidade não encontrada.");
        }
        if (pag.getValorPago().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Esta mensalidade não possui pagamento registrado para estorno.");
        }

        if (!pag.estornar(conexao)) {
            throw new IllegalStateException("Não foi possível estornar o pagamento.");
        }

        // O estorno só reabre a mensalidade: quem passa a dever aparece como pendente, e só o financeiro marca inadimplente
        return Map.of("mensagem", "Estorno concluído! A mensalidade " + pag.getReferencia() + " foi reaberta.");
    }
}