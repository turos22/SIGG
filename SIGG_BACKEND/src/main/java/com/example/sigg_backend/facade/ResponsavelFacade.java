package com.example.sigg_backend.facade;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Responsavel;
import com.example.sigg_backend.db.util.Conexao;

@Service
public class ResponsavelFacade {

    private final MembroFacade membroFacade;
    private final AutorizacaoFacade autorizacao;

    public ResponsavelFacade(MembroFacade membroFacade, AutorizacaoFacade autorizacao) {
        this.membroFacade = membroFacade;
        this.autorizacao = autorizacao;
    }

    // Quem é ligado a um escoteiro passa a ser responsável; ao sair do último vínculo, deixa de ser
    @Transactional
    public List<Responsavel> vincular(Conexao conexao, int memCodEscoteiro, int memCodResp) {
        autorizacao.exigirDiretoria(conexao);
        Escoteiro escoteiro = escoteiroExistente(conexao, memCodEscoteiro);
        Membro membro = membroFacade.buscarAtivo(conexao, memCodResp);
        if (memCodResp == memCodEscoteiro)
            throw new RegraNegocioException("O escoteiro não pode ser responsável por si mesmo");
        if (!membro.maiorDeIdade())
            throw new RegraNegocioException("Responsável deve ter 18 anos ou mais");
        Responsavel responsavel = new Responsavel();
        responsavel.setMembro(membro);
        if (responsavel.existeVinculo(conexao, memCodEscoteiro))
            throw new RegraNegocioException(membro.getNome() + " já é responsável por " + escoteiro.getMembro().getNome());
        if (!responsavel.existe(conexao, memCodResp))
            gravar(conexao, responsavel);
        if (!responsavel.vincular(conexao, memCodEscoteiro))
            throw new IllegalStateException("Não foi possível vincular o responsável");
        return responsavel.doEscoteiro(conexao, memCodEscoteiro);
    }

    @Transactional
    public List<Responsavel> desvincular(Conexao conexao, int memCodEscoteiro, int memCodResp) {
        autorizacao.exigirDiretoria(conexao);
        escoteiroExistente(conexao, memCodEscoteiro);
        Responsavel responsavel = new Responsavel();
        responsavel.setMembro(membroFacade.buscarAtivo(conexao, memCodResp));
        if (!responsavel.existeVinculo(conexao, memCodEscoteiro))
            throw new NaoEncontradoException("Esse responsável não está vinculado a esse escoteiro");
        if (!responsavel.desvincular(conexao, memCodEscoteiro))
            throw new IllegalStateException("Não foi possível desvincular o responsável");
        if (responsavel.quantosVinculos(conexao) == 0 && !responsavel.apagar(conexao))
            throw new IllegalStateException("Não foi possível retirar o papel de responsável");
        return responsavel.doEscoteiro(conexao, memCodEscoteiro);
    }

    private Escoteiro escoteiroExistente(Conexao conexao, int memCod) {
        Escoteiro escoteiro = new Escoteiro().buscar(conexao, memCod);
        if (escoteiro == null)
            throw new NaoEncontradoException("Escoteiro " + memCod + " não encontrado");
        return escoteiro;
    }

    @Transactional
    public Responsavel cadastrar(Conexao conexao, Responsavel responsavel) {
        membroFacade.validarNovo(conexao, responsavel.getMembro());
        gravar(conexao, responsavel);
        return responsavel;
    }

    @Transactional
    public Responsavel acrescentar(Conexao conexao, int memCod) {
        Membro membro = membroFacade.buscarAtivo(conexao, memCod);
        Responsavel responsavel = new Responsavel();
        if (responsavel.existe(conexao, memCod))
            throw new RuntimeException(membro.getNome() + " já é responsável");
        responsavel.setMembro(membro);
        gravar(conexao, responsavel);
        return responsavel;
    }

    private void gravar(Conexao conexao, Responsavel responsavel) {
        if (!responsavel.gravar(conexao))
            throw new IllegalStateException("Não foi possível gravar o responsável");
    }
}
