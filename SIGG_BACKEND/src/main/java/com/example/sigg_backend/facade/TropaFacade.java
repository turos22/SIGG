package com.example.sigg_backend.facade;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.models.Tropa;
import com.example.sigg_backend.models.UsuarioLogado;

@Service
public class TropaFacade {

    private static final List<String> GESTORES = List.of(UsuarioLogado.ADMIN, UsuarioLogado.PRESIDENTE);

    private final AutorizacaoFacade autorizacao;

    public TropaFacade(AutorizacaoFacade autorizacao) {
        this.autorizacao = autorizacao;
    }

    public Tropa buscar(Conexao conexao, int cod) {
        Tropa tropa = new Tropa().buscar(conexao, cod);
        if (tropa == null)
            throw new NaoEncontradoException("Tropa " + cod + " não encontrada");
        return tropa;
    }

    @Transactional
    public Tropa cadastrar(Conexao conexao, Tropa tropa) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        exigirNomeLivre(conexao, tropa, 0);
        if (!tropa.gravar(conexao))
            throw new IllegalStateException("Não foi possível gravar a tropa");
        return tropa;
    }

    @Transactional
    public Tropa alterar(Conexao conexao, int cod, Tropa dados) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        buscar(conexao, cod);
        exigirNomeLivre(conexao, dados, cod);
        if (!dados.alterar(conexao))
            throw new IllegalStateException("Não foi possível alterar a tropa");
        return dados;
    }

    @Transactional
    public Map<String, String> excluir(Conexao conexao, int cod) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        Tropa tropa = buscar(conexao, cod);
        if (tropa.temDependentes(conexao))
            throw new RegraNegocioException("Não é possível excluir a tropa: ela tem patrulhas ou chefes vinculados");
        if (!tropa.apagar(conexao))
            throw new IllegalStateException("Não foi possível excluir a tropa");
        return Map.of("mensagem", "Tropa " + tropa.getNome() + " excluída");
    }

    private void exigirNomeLivre(Conexao conexao, Tropa tropa, int cod) {
        tropa.setCod(cod);
        if (tropa.nomeJaUsadoPorOutra(conexao))
            throw new RegraNegocioException("Já existe uma tropa com esse nome");
    }
}
