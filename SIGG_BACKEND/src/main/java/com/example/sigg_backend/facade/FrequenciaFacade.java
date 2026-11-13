package com.example.sigg_backend.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.models.Chamada;
import com.example.sigg_backend.models.Chefe;
import com.example.sigg_backend.models.ItemChamada;
import com.example.sigg_backend.models.ResumoChamada;
import com.example.sigg_backend.models.Tropa;
import com.example.sigg_backend.models.UsuarioLogado;

@Service
public class FrequenciaFacade {

    private static final List<String> OPERADORES = List.of(UsuarioLogado.ADMIN, UsuarioLogado.PRESIDENTE, UsuarioLogado.CHEFE);
    private static final List<String> SEM_RESTRICAO = List.of(UsuarioLogado.ADMIN, UsuarioLogado.PRESIDENTE);
    private static final String SEM_REGISTRO = "Não há frequência dessa tropa nessa data";

    private final AutorizacaoFacade autorizacao;

    public FrequenciaFacade(AutorizacaoFacade autorizacao) {
        this.autorizacao = autorizacao;
    }

    public List<ResumoChamada> listar(Conexao conexao, Integer troCod, Integer ano) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        Integer tropa = troCod;
        Integer propria = tropaDoChefe(conexao);
        if (propria != null) // chefe só enxerga a própria tropa: sem filtro, usa a dele
            tropa = tropa == null ? propria : exigirTropa(conexao, tropa);
        return new ResumoChamada().listar(conexao, tropa, anoOuAtual(ano));
    }

    // Tropa do chefe logado (404 se não é chefe com gestão em andamento): o front trava o combo nela
    public Tropa minhaTropa(Conexao conexao) {
        UsuarioLogado usuario = autorizacao.exigirAutenticado(conexao);
        Integer cod = new Chefe().tropaAtiva(conexao, usuario.getMem_cod());
        if (cod == null)
            throw new NaoEncontradoException("Você não é chefe de nenhuma tropa");
        return tropaExistente(conexao, cod);
    }

    public Chamada consultar(Conexao conexao, int troCod, LocalDate data) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        exigirTropa(conexao, troCod);
        Chamada chamada = new Chamada();
        chamada.setTropa(tropaExistente(conexao, troCod));
        chamada.setData(data);
        return chamada.consultar(conexao);
    }

    public List<ItemChamada> percentuais(Conexao conexao, int troCod, Integer ano) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        exigirTropa(conexao, troCod);
        Chamada chamada = new Chamada();
        chamada.setTropa(tropaExistente(conexao, troCod));
        return chamada.percentuais(conexao, anoOuAtual(ano));
    }

    @Transactional
    public Chamada registrar(Conexao conexao, Chamada chamada) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        prepararChamada(conexao, chamada);
        if (chamada.temRegistro(conexao))
            throw new RegraNegocioException("Já existe frequência desta tropa nesta data. Edite a chamada existente.");
        return gravar(conexao, chamada);
    }

    @Transactional
    public Chamada atualizar(Conexao conexao, Chamada chamada) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        prepararChamada(conexao, chamada);
        if (!chamada.temRegistro(conexao))
            throw new NaoEncontradoException(SEM_REGISTRO);
        return gravar(conexao, chamada);
    }

    @Transactional
    public Map<String, String> excluir(Conexao conexao, int troCod, LocalDate data) {
        autorizacao.exigirPerfil(conexao, OPERADORES);
        exigirTropa(conexao, troCod);
        Chamada chamada = new Chamada();
        chamada.setTropa(tropaExistente(conexao, troCod));
        chamada.setData(data);
        if (!chamada.temRegistro(conexao))
            throw new NaoEncontradoException(SEM_REGISTRO);
        if (!chamada.apagar(conexao))
            throw new IllegalStateException("Não foi possível excluir a frequência");
        return Map.of("mensagem", "Frequência de " + chamada.getTropa().getNome() + " em " + data + " excluída");
    }

    private Chamada gravar(Conexao conexao, Chamada chamada) {
        if (!chamada.registrar(conexao))
            throw new IllegalStateException("Não foi possível gravar a frequência");
        return chamada.consultar(conexao);
    }

    // Data, status e itens já foram conferidos no controller: aqui a tropa e os escoteiros, que dependem do banco
    private void prepararChamada(Conexao conexao, Chamada chamada) {
        exigirTropa(conexao, chamada.getTropa().getCod());
        chamada.setTropa(tropaExistente(conexao, chamada.getTropa().getCod()));
        Integer intruso = chamada.primeiroEscoteiroDeOutraTropa(conexao);
        if (intruso != null)
            throw new RegraNegocioException("Escoteiro " + intruso + " não pertence a essa tropa");
    }

    // Chefe (que não é presidente nem admin) só opera a tropa da gestão em andamento, lida do banco a cada chamada:
    // o perfil da sessão só é calculado no login e não sabe que a gestão acabou
    private int exigirTropa(Conexao conexao, int troCod) {
        Integer propria = tropaDoChefe(conexao);
        if (propria != null && propria != troCod)
            throw new AcessoNegadoException("Você só pode registrar a frequência da sua tropa");
        return troCod;
    }

    // null = sem restrição de tropa (segurança desligada, admin, presidente) ou usuário que nem é chefe ativo
    private Integer tropaDoChefe(Conexao conexao) {
        if (!autorizacao.segurancaAtiva())
            return null;
        UsuarioLogado usuario = autorizacao.usuarioAtual();
        if (usuario.temAlgumPerfil(SEM_RESTRICAO))
            return null;
        Integer propria = new Chefe().tropaAtiva(conexao, usuario.getMem_cod());
        if (propria == null)
            throw new AcessoNegadoException("Você não é chefe de nenhuma tropa");
        return propria;
    }

    private Tropa tropaExistente(Conexao conexao, int troCod) {
        Tropa tropa = new Tropa().buscar(conexao, troCod);
        if (tropa == null)
            throw new NaoEncontradoException("Tropa " + troCod + " não encontrada");
        return tropa;
    }

    private int anoOuAtual(Integer ano) {
        return ano == null ? LocalDate.now().getYear() : ano;
    }
}
