package com.example.sigg_backend.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.models.Chamada;
import com.example.sigg_backend.models.ItemChamada;
import com.example.sigg_backend.models.ResumoChamada;
import com.example.sigg_backend.models.Tropa;
import com.example.sigg_backend.models.UsuarioLogado;

@Service
public class FrequenciaFacade {

    private static final List<String> OPERADORES = List.of(UsuarioLogado.ADMIN, UsuarioLogado.PRESIDENTE, UsuarioLogado.CHEFE);
    private static final String SEM_REGISTRO = "Não há frequência dessa tropa nessa data";

    private final AutorizacaoFacade autorizacao;

    public FrequenciaFacade(AutorizacaoFacade autorizacao) {
        this.autorizacao = autorizacao;
    }

    public List<ResumoChamada> listar(Conexao conexao, Integer troCod, Integer ano) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        return new ResumoChamada().listar(conexao, troCod, anoOuAtual(ano));
    }

    public Chamada consultar(Conexao conexao, int troCod, LocalDate data) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        Chamada chamada = new Chamada();
        chamada.setTropa(tropaExistente(conexao, troCod));
        chamada.setData(data);
        return chamada.consultar(conexao);
    }

    public List<ItemChamada> percentuais(Conexao conexao, int troCod, Integer ano) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        Chamada chamada = new Chamada();
        chamada.setTropa(tropaExistente(conexao, troCod));
        return chamada.percentuais(conexao, anoOuAtual(ano));
    }

    @Transactional
    public Chamada registrar(Conexao conexao, Chamada chamada) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        validar(conexao, chamada);
        if (chamada.temRegistro(conexao))
            throw new RegraNegocioException("Já existe frequência desta tropa nesta data. Edite a chamada existente.");
        return gravar(conexao, chamada);
    }

    @Transactional
    public Chamada atualizar(Conexao conexao, Chamada chamada) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        validar(conexao, chamada);
        if (!chamada.temRegistro(conexao))
            throw new NaoEncontradoException(SEM_REGISTRO);
        return gravar(conexao, chamada);
    }

    @Transactional
    public Map<String, String> excluir(Conexao conexao, int troCod, LocalDate data) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        Chamada chamada = new Chamada();
        chamada.setTropa(tropaExistente(conexao, troCod));
        chamada.setData(data);
        if (!chamada.temRegistro(conexao))
            throw new NaoEncontradoException(SEM_REGISTRO);
        if (!chamada.apagar(conexao))
            throw new IllegalStateException("Não foi possível excluir a frequência");
        return Map.of("mensagem", "Frequência de " + chamada.getTropa().getNome() + " em " + data + " excluída");
    }

    private Chamada gravar(Conexao conexao, Chamada chamada) {
        if (!chamada.registrar(conexao))
            throw new IllegalStateException("Não foi possível gravar a frequência");
        return chamada.consultar(conexao);
    }

    private void validar(Conexao conexao, Chamada chamada) {
        if (chamada == null)
            throw new RegraNegocioException("Os dados da chamada são obrigatórios");
        String problema = chamada.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        chamada.setTropa(tropaExistente(conexao, chamada.getTropa().getCod()));
        Integer intruso = chamada.primeiroEscoteiroDeOutraTropa(conexao);
        if (intruso != null)
            throw new RegraNegocioException("Escoteiro " + intruso + " não pertence a essa tropa");
    }

    private Tropa tropaExistente(Conexao conexao, int troCod) {
        Tropa tropa = new Tropa().buscar(conexao, troCod);
        if (tropa == null)
            throw new NaoEncontradoException("Tropa " + troCod + " não encontrada");
        return tropa;
    }

    private int anoOuAtual(Integer ano) {
        return ano == null ? LocalDate.now().getYear() : ano;
    }
}
