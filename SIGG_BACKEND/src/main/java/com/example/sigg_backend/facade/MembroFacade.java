package com.example.sigg_backend.facade;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.db.util.Conexao;

@Service
public class MembroFacade {

    private final AutorizacaoFacade autorizacao;

    public MembroFacade(AutorizacaoFacade autorizacao) {
        this.autorizacao = autorizacao;
    }

    public Membro buscarAtivo(Conexao conexao, int memCod) {
        Membro membro = new Membro().buscar(conexao, memCod);
        if (membro == null)
            throw new NaoEncontradoException("Membro " + memCod + " não encontrado");
        return membro;
    }

    public Membro buscar(Conexao conexao, int memCod) {
        Membro membro = new Membro().buscarComPapeis(conexao, memCod);
        if (membro == null)
            throw new NaoEncontradoException("Membro " + memCod + " não encontrado");
        return membro;
    }

    // Os campos já foram conferidos no controller; aqui o CPF único, que depende do banco
    public void exigirCpfLivre(Conexao conexao, Membro membro) {
        if (membro.cpfJaCadastrado(conexao))
            throw new RegraNegocioException(
                    "Já existe um membro com esse CPF. Para dar outro papel a ele, use POST /membros/{id}/...");
        membro.setMem_cod(0);
    }

    @Transactional
    public Membro cadastrar(Conexao conexao, Membro membro) {
        autorizacao.exigirDiretoria(conexao);
        exigirCpfLivre(conexao, membro);
        if (!membro.gravar(conexao))
            throw new IllegalStateException("Não foi possível gravar o membro");
        return buscar(conexao, membro.getMem_cod());
    }

    @Transactional
    public Membro alterar(Conexao conexao, int memCod, Membro dados) {
        autorizacao.exigirDiretoria(conexao);
        Membro atual = buscarAtivo(conexao, memCod);
        dados.setMem_cod(memCod);
        if (dados.cpfJaCadastradoEmOutro(conexao))
            throw new RegraNegocioException("Já existe outro membro com esse CPF");
        dados.setDt_ini(atual.getDt_ini());
        dados.setDt_fim(null);
        if (!dados.alterar(conexao))
            throw new IllegalStateException("Não foi possível alterar o membro");
        return buscar(conexao, memCod);
    }

    @Transactional
    public Map<String, String> excluir(Conexao conexao, int memCod) {
        autorizacao.exigirDiretoria(conexao);
        Membro membro = buscarAtivo(conexao, memCod);
        if (!membro.apagar(conexao))
            throw new IllegalStateException("Não foi possível excluir o membro");
        return Map.of("mensagem", membro.getNome() + " foi removido(a) do grupo");
    }
}
