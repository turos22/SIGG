package com.example.sigg_backend.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.CategoriaMensalidade;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Mensalidade;
import com.example.sigg_backend.models.Responsavel;
import com.example.sigg_backend.models.SaldoAdotado;
import com.example.sigg_backend.models.SaldoMensalidades;
import com.example.sigg_backend.models.UsuarioLogado;
import com.example.sigg_backend.db.util.Conexao;

@Service
public class MensalidadeFacade {

    private final AutorizacaoFacade autorizacao;

    public MensalidadeFacade(AutorizacaoFacade autorizacao) {
        this.autorizacao = autorizacao;
    }

    public List<Escoteiro> pesquisarEscoteiros(Conexao conexao, String busca) {
        autorizacao.exigirDiretoria(conexao);
        if (busca == null || busca.isBlank())
            throw new RegraNegocioException("Informe CPF, nome ou matrícula do escoteiro");
        String termo = busca.trim();
        if (termo.matches("[\\d.\\-]+"))
            termo = termo.replaceAll("\\D", "");
        List<Escoteiro> escoteiros = new Escoteiro().pesquisar(conexao, termo);
        if (escoteiros.isEmpty())
            throw new NaoEncontradoException("Escoteiro não localizado no sistema");
        return escoteiros;
    }

    public SaldoMensalidades calcular(Conexao conexao, int memCod) {
        autorizacao.exigirAcessoAoEscoteiro(conexao, memCod);
        Escoteiro escoteiro = new Escoteiro().buscar(conexao, memCod);
        if (escoteiro == null)
            throw new NaoEncontradoException("Escoteiro " + memCod + " não encontrado");
        return montarSaldo(conexao, escoteiro);
    }

    public List<Escoteiro> dependentesDoResponsavel(Conexao conexao, int memCodResponsavel) {
        autorizacao.exigirAcessoAoResponsavel(conexao, memCodResponsavel);
        if (!new Responsavel().existe(conexao, memCodResponsavel))
            throw new NaoEncontradoException("Responsável " + memCodResponsavel + " não encontrado");
        List<Escoteiro> perfis = new ArrayList<>(new Escoteiro().doResponsavel(conexao, memCodResponsavel));
        Escoteiro proprio = new Escoteiro().buscar(conexao, memCodResponsavel);
        if (proprio != null) {
            for (Escoteiro adotado : proprio.adotados(conexao)) {
                boolean jaListado = perfis.stream().anyMatch(perfil -> perfil.getEsc_cod() == adotado.getEsc_cod());
                if (!jaListado)
                    perfis.add(adotado);
            }
        }
        return perfis;
    }

    public List<SaldoMensalidades> calcularDoResponsavel(Conexao conexao, int memCodResponsavel) {
        List<SaldoMensalidades> saldos = new ArrayList<>();
        for (Escoteiro perfil : dependentesDoResponsavel(conexao, memCodResponsavel))
            saldos.add(montarSaldo(conexao, perfil));
        return saldos;
    }

    @Transactional
    public Map<String, Object> lancarMes(Conexao conexao, int mes, int ano) {
        autorizacao.exigirPerfil(conexao, List.of(UsuarioLogado.ADMIN, UsuarioLogado.FINANCEIRO));
        if (mes < 1 || mes > 12)
            throw new RegraNegocioException("Mês deve ser de 1 a 12");
        if (ano < 2000 || ano > 2100)
            throw new RegraNegocioException("Ano inválido");
        Map<Integer, CategoriaMensalidade> categorias = new HashMap<>();
        int lancadas = 0;
        int ignoradas = 0;
        for (Escoteiro escoteiro : new Escoteiro().listar(conexao)) {
            CategoriaMensalidade categoria = categorias.get(escoteiro.getCat_mensalidade());
            if (categoria == null) {
                categoria = new CategoriaMensalidade().buscarPorId(conexao, escoteiro.getCat_mensalidade());
                if (categoria == null)
                    throw new RegraNegocioException("Categoria de mensalidade " + escoteiro.getCat_mensalidade()
                            + " de " + escoteiro.getMembro().getNome() + " não encontrada");
                categorias.put(escoteiro.getCat_mensalidade(), categoria);
            }
            if (categoria.getValor().signum() <= 0 || new Mensalidade().existe(conexao, escoteiro.getEsc_cod(), mes, ano)) {
                ignoradas++;
                continue;
            }
            Mensalidade mensalidade = new Mensalidade();
            mensalidade.setMens_mes(mes);
            mensalidade.setMens_ano(ano);
            mensalidade.setMens_valor(categoria.getValor());
            mensalidade.setEsc_cod(escoteiro.getEsc_cod());
            mensalidade.setCat_mensalidade(categoria.getCat_mens_cod());
            String problema = mensalidade.validar();
            if (problema != null)
                throw new RegraNegocioException(problema);
            if (!mensalidade.gravar(conexao))
                throw new IllegalStateException("Não foi possível lançar a mensalidade de " + escoteiro.getMembro().getNome());
            lancadas++;
        }
        return Map.of("mes", mes, "ano", ano, "lancadas", lancadas, "ignoradas", ignoradas);
    }

    private SaldoMensalidades montarSaldo(Conexao conexao, Escoteiro escoteiro) {
        SaldoMensalidades saldo = new SaldoMensalidades();
        saldo.identificar(escoteiro);
        if (escoteiro.ehAdotado()) {
            Escoteiro anjo = escoteiro.buscarPorCodigo(conexao, escoteiro.getAnjo());
            saldo.apadrinhar(anjo == null ? "do grupo" : anjo.getMembro().getNome());
        }
        List<SaldoAdotado> saldosDosAdotados = new ArrayList<>();
        for (Escoteiro adotado : escoteiro.adotados(conexao)) {
            SaldoAdotado saldoAdotado = new SaldoAdotado();
            saldoAdotado.setMem_cod(adotado.getMembro().getMem_cod());
            saldoAdotado.setEsc_cod(adotado.getEsc_cod());
            saldoAdotado.setNome(adotado.getMembro().getNome());
            saldoAdotado.calcular(new Mensalidade().doEscoteiro(conexao, adotado.getEsc_cod()));
            saldosDosAdotados.add(saldoAdotado);
        }
        saldo.calcular(new Mensalidade().doEscoteiro(conexao, escoteiro.getEsc_cod()), saldosDosAdotados);
        return saldo;
    }
}
