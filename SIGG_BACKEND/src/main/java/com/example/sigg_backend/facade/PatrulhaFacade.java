package com.example.sigg_backend.facade;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Patrulha;
import com.example.sigg_backend.models.Tropa;
import com.example.sigg_backend.models.UsuarioLogado;

@Service
public class PatrulhaFacade {

    private static final List<String> GESTORES = List.of(UsuarioLogado.ADMIN, UsuarioLogado.PRESIDENTE);

    private final AutorizacaoFacade autorizacao;

    public PatrulhaFacade(AutorizacaoFacade autorizacao) {
        this.autorizacao = autorizacao;
    }

    public Patrulha buscar(Conexao conexao, int cod) {
        Patrulha patrulha = new Patrulha().buscar(conexao, cod);
        if (patrulha == null)
            throw new NaoEncontradoException("Patrulha " + cod + " não encontrada");
        return patrulha;
    }

    @Transactional
    public Patrulha cadastrar(Conexao conexao, Patrulha patrulha) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        validar(conexao, patrulha, 0);
        if (!patrulha.gravar(conexao))
            throw new IllegalStateException("Não foi possível gravar a patrulha");
        return buscar(conexao, patrulha.getCod());
    }

    @Transactional
    public Patrulha alterar(Conexao conexao, int cod, Patrulha dados) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        buscar(conexao, cod);
        validar(conexao, dados, cod);
        if (!dados.alterar(conexao))
            throw new IllegalStateException("Não foi possível alterar a patrulha");
        return buscar(conexao, cod);
    }

    @Transactional
    public Map<String, String> excluir(Conexao conexao, int cod) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        Patrulha patrulha = buscar(conexao, cod);
        if (patrulha.temEscoteiros(conexao))
            throw new RegraNegocioException("Não é possível excluir a patrulha: ela tem escoteiros");
        if (!patrulha.apagar(conexao))
            throw new IllegalStateException("Não foi possível excluir a patrulha");
        return Map.of("mensagem", "Patrulha " + patrulha.getNome() + " excluída");
    }

    private void validar(Conexao conexao, Patrulha patrulha, int cod) {
        if (patrulha == null)
            throw new RegraNegocioException("Os dados da patrulha são obrigatórios");
        if (patrulha.getNome() != null)
            patrulha.setNome(patrulha.getNome().trim());
        if (semCodigo(patrulha.getLider()))
            patrulha.setLider(null);
        if (semCodigo(patrulha.getSublider()))
            patrulha.setSublider(null);
        String problema = patrulha.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        Tropa tropa = new Tropa().buscar(conexao, patrulha.getTropa().getCod());
        if (tropa == null)
            throw new NaoEncontradoException("Tropa " + patrulha.getTropa().getCod() + " não encontrada");
        patrulha.setTropa(tropa);
        patrulha.setCod(cod);
        if (patrulha.getLider() != null && !patrulha.escoteiroEhDaTropa(conexao, patrulha.getLider().getEsc_cod()))
            throw new RegraNegocioException("Líder deve ser escoteiro de uma patrulha da mesma tropa");
        if (patrulha.getSublider() != null && !patrulha.escoteiroEhDaTropa(conexao, patrulha.getSublider().getEsc_cod()))
            throw new RegraNegocioException("Sublíder deve ser escoteiro de uma patrulha da mesma tropa");
    }

    private boolean semCodigo(Escoteiro escoteiro) {
        return escoteiro != null && escoteiro.getEsc_cod() <= 0;
    }
}
