package com.example.sigg_backend.facade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.sigg_backend.models.Conta;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.UsuarioLogado;
import com.example.sigg_backend.db.util.Conexao;

@Service
public class AutorizacaoFacade {

    private static final String MENSAGEM_NEGADO = "Seu perfil não tem permissão para acessar estes dados";
    private static final String MENSAGEM_NAO_AUTENTICADO = "Autenticação necessária";
    private static final String MENSAGEM_CONTA_INATIVA = "Conta inativa. Procure um administrador do grupo";

    private final boolean ativa;
    private final SessoesAtivas sessoes;

    public AutorizacaoFacade(@Value("${sigg.seguranca.ativa:true}") boolean ativa, SessoesAtivas sessoes) {
        this.ativa = ativa;
        this.sessoes = sessoes;
    }

    public boolean segurancaAtiva() {
        return ativa;
    }

    public UsuarioLogado usuarioAtual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.getPrincipal() instanceof UsuarioLogado usuario)
            return usuario;
        return null;
    }

    public UsuarioLogado exigirAutenticado(Conexao conexao) {
        UsuarioLogado usuario = usuarioAtual();
        if (usuario == null)
            throw new NaoAutenticadoException(MENSAGEM_NAO_AUTENTICADO);
        if (ativa && !contaContinuaAtiva(conexao, usuario.getMem_cod())) {
            sessoes.encerrarTodas(usuario.getName());
            throw new NaoAutenticadoException(MENSAGEM_CONTA_INATIVA);
        }
        return usuario;
    }

    public void exigirPerfil(Conexao conexao, List<String> perfisPermitidos) {
        if (!ativa)
            return;
        if (!exigirAutenticado(conexao).temAlgumPerfil(perfisPermitidos))
            throw new AcessoNegadoException(MENSAGEM_NEGADO);
    }

    public void exigirDiretoria(Conexao conexao) {
        exigirPerfil(conexao, UsuarioLogado.DIRETORIA);
    }

    public void exigirAcessoAoEscoteiro(Conexao conexao, int memCodEscoteiro) {
        if (!ativa)
            return;
        UsuarioLogado usuario = exigirAutenticado(conexao);
        if (usuario.ehDiretoria() || memCodEscoteiro == usuario.getMem_cod())
            return;
        if (usuario.temPerfil(UsuarioLogado.RESPONSAVEL) && perfisSobResponsabilidade(conexao, usuario).contains(memCodEscoteiro))
            return;
        throw new AcessoNegadoException(MENSAGEM_NEGADO);
    }

    public void exigirAcessoAoResponsavel(Conexao conexao, int memCodResponsavel) {
        if (!ativa)
            return;
        UsuarioLogado usuario = exigirAutenticado(conexao);
        if (usuario.ehDiretoria())
            return;
        if (usuario.temPerfil(UsuarioLogado.RESPONSAVEL) && memCodResponsavel == usuario.getMem_cod())
            return;
        throw new AcessoNegadoException(MENSAGEM_NEGADO);
    }

    private boolean contaContinuaAtiva(Conexao conexao, int memCod) {
        Conta conta = new Conta().buscarPorMembro(conexao, memCod);
        return conta != null && conta.existe() && conta.estaAtiva();
    }

    private Set<Integer> perfisSobResponsabilidade(Conexao conexao, UsuarioLogado usuario) {
        Set<Integer> permitidos = new HashSet<>();
        for (Escoteiro dependente : new Escoteiro().doResponsavel(conexao, usuario.getMem_cod()))
            permitidos.add(dependente.getMembro().getMem_cod());
        Escoteiro proprio = new Escoteiro().buscar(conexao, usuario.getMem_cod());
        if (proprio != null)
            for (Escoteiro adotado : proprio.adotados(conexao))
                permitidos.add(adotado.getMembro().getMem_cod());
        return permitidos;
    }
}
