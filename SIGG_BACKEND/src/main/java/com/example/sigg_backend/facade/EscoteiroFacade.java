package com.example.sigg_backend.facade;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.CategoriaMensalidade;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Patrulha;
import com.example.sigg_backend.db.util.Conexao;


@Service
public class EscoteiroFacade {

    private final MembroFacade membroFacade;
    private final AutorizacaoFacade autorizacao;

    public EscoteiroFacade(MembroFacade membroFacade, AutorizacaoFacade autorizacao) {
        this.membroFacade = membroFacade;
        this.autorizacao = autorizacao;
    }

    public Escoteiro buscar(Conexao conexao, int memCod) {
        Escoteiro escoteiro = new Escoteiro().buscar(conexao, memCod);
        if (escoteiro == null)
            throw new NaoEncontradoException("Escoteiro " + memCod + " não encontrado");
        return escoteiro;
    }

    @Transactional
    public Escoteiro cadastrar(Conexao conexao, Escoteiro escoteiro) {
        membroFacade.validarNovo(conexao, escoteiro.getMembro());
        validar(conexao, escoteiro);
        gravar(conexao, escoteiro);
        return escoteiro;
    }

    @Transactional
    public Escoteiro acrescentar(Conexao conexao, int memCod, Escoteiro dados) {
        Membro membro = membroFacade.buscarAtivo(conexao, memCod);
        if (dados.existe(conexao, memCod))
            throw new RuntimeException(membro.getNome() + " já é escoteiro");
        dados.setMembro(membro);
        validar(conexao, dados);
        gravar(conexao, dados);
        return dados;
    }

    @Transactional
    public Escoteiro alterar(Conexao conexao, int memCod, Escoteiro dados) {
        autorizacao.exigirDiretoria(conexao);
        Escoteiro atual = buscar(conexao, memCod);
        if (dados == null)
            throw new RegraNegocioException("Os dados do escoteiro são obrigatórios");
        dados.setMembro(atual.getMembro());
        validar(conexao, dados);
        if (!dados.alterarCadastro(conexao))
            throw new IllegalStateException("Não foi possível alterar o escoteiro");
        return buscar(conexao, memCod);
    }

    private void validar(Conexao conexao, Escoteiro escoteiro) {
        String problema = escoteiro.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        if (CategoriaMensalidade.buscarPorId(conexao, escoteiro.getCat_mensalidade()) == null)
            throw new RegraNegocioException("Categoria de mensalidade " + escoteiro.getCat_mensalidade() + " não existe");
        if (new Patrulha().buscar(conexao, escoteiro.getPatrulha().getCod()) == null)
            throw new NaoEncontradoException("Patrulha " + escoteiro.getPatrulha().getCod() + " não encontrada");
    }

    private void gravar(Conexao conexao, Escoteiro escoteiro) {
        if (!escoteiro.gravar(conexao))
            throw new IllegalStateException("Não foi possível gravar o escoteiro");
    }
}
