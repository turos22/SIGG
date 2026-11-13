package com.example.sigg_backend.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.Membro;
import com.example.sigg_backend.models.Presidente;
import com.example.sigg_backend.models.UsuarioLogado;

@Service
public class PresidenteFacade {

    private static final List<String> GESTORES = List.of(UsuarioLogado.ADMIN, UsuarioLogado.PRESIDENTE);

    private final MembroFacade membroFacade;
    private final AutorizacaoFacade autorizacao;

    public PresidenteFacade(MembroFacade membroFacade, AutorizacaoFacade autorizacao) {
        this.membroFacade = membroFacade;
        this.autorizacao = autorizacao;
    }

    // Troca de posse: quem exerce o cargo termina na véspera da posse de quem entra
    @Transactional
    public Presidente promover(Conexao conexao, int memCod, Presidente dados) {
        autorizacao.exigirPerfil(conexao, GESTORES);
        Membro membro = membroFacade.buscarAtivo(conexao, memCod);
        if (!new Escoteiro().existe(conexao, memCod))
            throw new RegraNegocioException("Só um escoteiro pode ser promovido a presidente");
        Presidente atual = dados.buscar(conexao, memCod);
        if (atual != null && atual.estaVigente())
            throw new RegraNegocioException(membro.getNome() + " já é presidente");
        encerrarVigentes(conexao, dados.getGestao_ini().minusDays(1));
        dados.setMembro(membro);
        // quem já foi presidente volta pela mesma linha (mem_cod é a chave da tabela)
        boolean gravou = atual == null ? dados.gravar(conexao) : dados.atualizarGestao(conexao);
        if (!gravou)
            throw new IllegalStateException("Não foi possível gravar o presidente");
        return dados.buscar(conexao, memCod); // devolve completo: membro, escoteiro e cargo
    }

    // Só encurta: uma gestão que já acaba antes da posse fica como está
    private void encerrarVigentes(Conexao conexao, LocalDate vespera) {
        for (Presidente vigente : new Presidente().vigentes(conexao)) {
            if (!vigente.getGestao_fim().isAfter(vespera))
                continue;
            if (vespera.isBefore(vigente.getGestao_ini()))
                throw new RegraNegocioException("A posse deve ser depois do início da gestão do presidente atual");
            if (!vigente.encerrarGestao(conexao, vespera))
                throw new IllegalStateException("Não foi possível encerrar a gestão do presidente atual");
        }
    }
}
