package com.example.sigg_backend.facade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.UsuarioLogado;

@Service
public class AutorizacaoFacade {

    private static final String MENSAGEM_NEGADO = "Seu perfil não tem permissão para acessar estes dados";
    private static final String MENSAGEM_NAO_AUTENTICADO = "Autenticação necessária";

    private final boolean ativa;

    public AutorizacaoFacade(@Value("${sigg.seguranca.ativa:true}") boolean ativa) {
        this.ativa = ativa;
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

    public UsuarioLogado exigirAutenticado() {
        UsuarioLogado usuario = usuarioAtual();
        if (usuario == null)
            throw new NaoAutenticadoException(MENSAGEM_NAO_AUTENTICADO);
        return usuario;
    }

    public void exigirPerfil(List<String> perfisPermitidos) {
        if (!ativa)
            return;
        if (!exigirAutenticado().temAlgumPerfil(perfisPermitidos))
            throw new AcessoNegadoException(MENSAGEM_NEGADO);
    }

    public void exigirDiretoria() {
        exigirPerfil(UsuarioLogado.DIRETORIA);
    }

    public void exigirAcessoAoEscoteiro(int memCodEscoteiro) {
        if (!ativa)
            return;
        UsuarioLogado usuario = exigirAutenticado();
        if (usuario.ehDiretoria() || memCodEscoteiro == usuario.getMem_cod())
            return;
        if (usuario.temPerfil(UsuarioLogado.RESPONSAVEL) && perfisSobResponsabilidade(usuario).contains(memCodEscoteiro))
            return;
        throw new AcessoNegadoException(MENSAGEM_NEGADO);
    }

    public void exigirAcessoAoResponsavel(int memCodResponsavel) {
        if (!ativa)
            return;
        UsuarioLogado usuario = exigirAutenticado();
        if (usuario.ehDiretoria())
            return;
        if (usuario.temPerfil(UsuarioLogado.RESPONSAVEL) && memCodResponsavel == usuario.getMem_cod())
            return;
        throw new AcessoNegadoException(MENSAGEM_NEGADO);
    }

    private Set<Integer> perfisSobResponsabilidade(UsuarioLogado usuario) {
        Set<Integer> permitidos = new HashSet<>();
        for (Escoteiro dependente : new Escoteiro().doResponsavel(usuario.getMem_cod()))
            permitidos.add(dependente.getMembro().getMem_cod());
        Escoteiro proprio = new Escoteiro().buscar(usuario.getMem_cod());
        if (proprio != null)
            for (Escoteiro adotado : proprio.adotados())
                permitidos.add(adotado.getMembro().getMem_cod());
        return permitidos;
    }
}
