package com.example.sigg_backend.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Parametrizacao;
import com.example.sigg_backend.models.UsuarioLogado;
import com.example.sigg_backend.db.util.Conexao;

// Parametrização do sistema: qualquer um lê (a tela de login já mostra nome e logo),
// só o administrador altera.
@Service
public class ParametrizacaoFacade {

    private final AutorizacaoFacade autorizacao;

    public ParametrizacaoFacade(AutorizacaoFacade autorizacao) {
        this.autorizacao = autorizacao;
    }

    public Parametrizacao buscar(Conexao conexao) {
        Parametrizacao parametrizacao = new Parametrizacao().buscar(conexao);
        if (parametrizacao == null)
            throw new NaoEncontradoException("Parametrização do sistema não encontrada no banco");
        return parametrizacao;
    }

    @Transactional
    public Parametrizacao alterar(Conexao conexao, Parametrizacao parametrizacao) {
        autorizacao.exigirPerfil(conexao, List.of(UsuarioLogado.ADMIN));
        if (parametrizacao == null)
            throw new RegraNegocioException("Os dados da parametrização são obrigatórios");
        buscar(conexao);
        parametrizacao.normalizar();
        String problema = parametrizacao.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        if (!parametrizacao.alterar(conexao))
            throw new IllegalStateException("Não foi possível alterar a parametrização");
        return parametrizacao;
    }
}
