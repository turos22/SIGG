package com.example.sigg_backend.facade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sigg_backend.models.Contribuicao;
import com.example.sigg_backend.models.Escoteiro;
import com.example.sigg_backend.models.RegistroContribuicao;

@Service
public class ContribuicaoFacade {

    private final AutorizacaoFacade autorizacao;

    public ContribuicaoFacade(AutorizacaoFacade autorizacao) {
        this.autorizacao = autorizacao;
    }

    private static final Logger LOG = LoggerFactory.getLogger(ContribuicaoFacade.class);

    public List<Contribuicao> listar() {
        autorizacao.exigirDiretoria();
        return new Contribuicao().listar();
    }

    public List<Contribuicao> listarDoAnjo(int memCod) {
        autorizacao.exigirDiretoria();
        Escoteiro anjo = new Escoteiro().buscar(memCod);
        if (anjo == null)
            throw new NaoEncontradoException("Escoteiro " + memCod + " não encontrado");
        return new Contribuicao().doAnjo(anjo.getEsc_cod());
    }

    public Map<String, Escoteiro> verificar(String cpfAnjo, String cpfAdotado) {
        autorizacao.exigirDiretoria();
        Escoteiro anjo = localizar(cpfAnjo, "O Anjo");
        Escoteiro adotado = localizar(cpfAdotado, "O adotado");
        validarVinculo(anjo, adotado);
        return Map.of("anjo", anjo, "adotado", adotado);
    }

    @Transactional
    public List<Contribuicao> registrar(RegistroContribuicao registro) {
        autorizacao.exigirDiretoria();
        if (registro == null)
            throw new RegraNegocioException("Os dados da contribuição são obrigatórios");
        String problema = registro.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        Escoteiro anjo = localizar(registro.getCpf_anjo(), "O Anjo");
        List<Contribuicao> registradas = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        for (String cpf : registro.getCpfs_adotados()) {
            String numeros = normalizar(cpf);
            if (!vistos.add(numeros))
                throw new RegraNegocioException("CPF " + numeros + " repetido na lista de adotados");
            Escoteiro adotado = localizar(numeros, "O adotado");
            validarVinculo(anjo, adotado);
            registradas.add(vincular(anjo, adotado));
        }
        return registradas;
    }

    private Contribuicao vincular(Escoteiro anjo, Escoteiro adotado) {
        if (!adotado.vincularAnjo(anjo.getEsc_cod())) {
            LOG.error("Falha ao vincular o adotado {} ao Anjo {}", adotado.getEsc_cod(), anjo.getEsc_cod());
            throw new IllegalStateException("Erro ao vincular as mensalidades de "
                    + adotado.getMembro().getNome() + " ao cadastro de " + anjo.getMembro().getNome());
        }
        Contribuicao contribuicao = new Contribuicao();
        contribuicao.setAnjo(anjo.getEsc_cod());
        contribuicao.setAdotado(adotado.getEsc_cod());
        contribuicao.setCon_dt(LocalDate.now());
        contribuicao.setNome_anjo(anjo.getMembro().getNome());
        contribuicao.setNome_adotado(adotado.getMembro().getNome());
        String problema = contribuicao.validar();
        if (problema != null)
            throw new RegraNegocioException(problema);
        if (!contribuicao.gravar()) {
            LOG.error("Falha ao registrar a contribuição do Anjo {} para o adotado {}",
                    anjo.getEsc_cod(), adotado.getEsc_cod());
            throw new IllegalStateException("Erro ao registrar a contribuição de " + anjo.getMembro().getNome());
        }
        return contribuicao;
    }

    private Escoteiro localizar(String cpf, String papel) {
        Escoteiro escoteiro = new Escoteiro().buscarPorCpf(normalizar(cpf));
        if (escoteiro == null)
            throw new NaoEncontradoException(papel + " não está cadastrado");
        return escoteiro;
    }

    private String normalizar(String cpf) {
        String numeros = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (!numeros.matches("\\d{11}"))
            throw new RegraNegocioException("CPF deve ter 11 dígitos, só números");
        return numeros;
    }

    private void validarVinculo(Escoteiro anjo, Escoteiro adotado) {
        if (anjo.getEsc_cod() == adotado.getEsc_cod())
            throw new RegraNegocioException("O Anjo não pode apadrinhar a si mesmo");
        if (anjo.ehAdotado())
            throw new RegraNegocioException(anjo.getMembro().getNome() + " é adotado e não pode ser Anjo");
        if (adotado.ehAdotado()) {
            if (adotado.getAnjo() == anjo.getEsc_cod())
                throw new RegraNegocioException(adotado.getMembro().getNome() + " já é adotado por este Anjo");
            throw new RegraNegocioException(adotado.getMembro().getNome() + " já possui um Anjo");
        }
        if (!adotado.adotados().isEmpty())
            throw new RegraNegocioException(adotado.getMembro().getNome() + " é Anjo de outros escoteiros e não pode ser adotado");
    }
}
