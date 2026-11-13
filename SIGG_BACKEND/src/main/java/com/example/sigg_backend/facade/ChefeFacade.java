package com.example.sigg_backend.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.models.Chefe;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Tropa;
import com.example.sigg_backend.models.UsuarioLogado;

@Service
public class ChefeFacade {

    private static final List<String> GESTORES = List.of(UsuarioLogado.ADMIN, UsuarioLogado.PRESIDENTE);

    private final MembroFacade membroFacade;
    private final AutorizacaoFacade autorizacao;

    public ChefeFacade(MembroFacade membroFacade, AutorizacaoFacade autorizacao) {
        this.membroFacade = membroFacade;
        this.autorizacao = autorizacao;
    }

    @Transactional
    public Chefe promover(Conexao conexao, int memCod, Chefe dados) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        Membro membro = membroFacade.buscarAtivo(conexao, memCod);
        if (!new Escoteiro().existe(conexao, memCod))
            throw new RegraNegocioException("Só um escoteiro pode ser promovido a chefe");
        Chefe atual = dados.buscar(conexao, memCod);
        if (atual != null && atual.estaAtivo())
            throw new RegraNegocioException(membro.getNome() + " já é chefe");
        if (new Tropa().buscar(conexao, dados.getTropa().getCod()) == null)
            throw new NaoEncontradoException("Tropa " + dados.getTropa().getCod() + " não encontrada");
        dados.setMembro(membro);
        // quem já foi chefe volta pela mesma linha (mem_cod é a chave da tabela)
        boolean gravou = atual == null ? dados.gravar(conexao) : dados.reativar(conexao);
        if (!gravou)
            throw new IllegalStateException("Não foi possível gravar o chefe");
        return dados.buscar(conexao, memCod); // devolve completo: membro, escoteiro, cargo e nome da tropa
    }

    // A gestão termina ontem: o fim é inclusivo (login e lista de membros usam "fim >= hoje"),
    // então terminar hoje deixaria a pessoa como chefe até amanhã
    @Transactional
    public Map<String, String> encerrar(Conexao conexao, int memCod) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        Chefe chefe = new Chefe().buscar(conexao, memCod);
        if (chefe == null || !chefe.estaAtivo())
            throw new NaoEncontradoException("Essa pessoa não é chefe");
        if (!chefe.encerrar(conexao, LocalDate.now().minusDays(1)))
            throw new IllegalStateException("Não foi possível encerrar a gestão do chefe");
        return Map.of("mensagem", chefe.getMembro().getNome() + " deixou de ser chefe");
    }
}
