package com.example.sigg_backend.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.AtividadePaga;
import com.example.sigg_backend.models.UsuarioLogado;
import com.example.sigg_backend.db.util.Conexao;

// Caso de uso RF_F5 "Ofertar Novas Atividades Pagas" (ator: Presidente).
@Service
public class AtividadePagaFacade {

    private static final List<String> QUEM_OFERTA = List.of(UsuarioLogado.ADMIN, UsuarioLogado.PRESIDENTE);

    private final AutorizacaoFacade autorizacao;

    public AtividadePagaFacade(AutorizacaoFacade autorizacao) {
        this.autorizacao = autorizacao;
    }

    // Passo 2: o sistema busca e exibe as atividades pagas já cadastradas.
    public List<AtividadePaga> listar(Conexao conexao, boolean somenteAbertas) {
        autorizacao.exigirDiretoria(conexao);
        return new AtividadePaga().listar(conexao, somenteAbertas);
    }

    public AtividadePaga buscar(Conexao conexao, int codigo) {
        autorizacao.exigirDiretoria(conexao);
        AtividadePaga atividade = new AtividadePaga().buscar(conexao, codigo);
        if (atividade == null)
            throw new NaoEncontradoException("Atividade paga " + codigo + " não encontrada");
        return atividade;
    }

    // Passos 5 e 6: valida, registra e deixa disponível na lista de atividades abertas.
    @Transactional
    public AtividadePaga ofertar(Conexao conexao, AtividadePaga atividade) {
        autorizacao.exigirPerfil(conexao, QUEM_OFERTA);
        if (atividade == null)
            throw new RegraNegocioException("Os dados da atividade são obrigatórios");
        atividade.normalizar();
        String problema = atividade.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        LocalDate hoje = LocalDate.now();
        if (atividade.getData().isBefore(hoje))
            throw new RegraNegocioException("A data de realização não pode estar no passado");
        if (atividade.getPrazoInteresse().isBefore(hoje))
            throw new RegraNegocioException("O prazo para manifestação de interesse não pode estar no passado");
        if (atividade.jaCadastrada(conexao))
            throw new RegraNegocioException(
                    "Já existe uma atividade paga cadastrada com o mesmo nome e data de realização");
        if (!atividade.gravar(conexao))
            throw new IllegalStateException("Não foi possível registrar a atividade paga");
        return atividade;
    }
}
