package com.example.sigg_backend.models;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.sigg_backend.db.dao.ContaDAO;
import com.example.sigg_backend.db.util.Conexao;

public class Conta {
    public static final String ATIVA = "A";
    public static final String PENDENTE = "P";
    public static final String INATIVA = "I";
    public static final int MAX_TENTATIVAS = 5;
    public static final int MINUTOS_DE_BLOQUEIO = 15;
    public static final int IDADE_MINIMA = 18;

    private static final BCryptPasswordEncoder CODIFICADOR = new BCryptPasswordEncoder();
    private static final String HASH_FALSO = CODIFICADOR.encode("senha-inexistente-para-igualar-o-tempo");

    private int cta_cod;
    private Membro membro;
    private String hash;
    private String situacao;
    private boolean admin;
    private int tentativas;
    private LocalDateTime bloqueio_ate;
    private LocalDate dt_criacao;
    private List<String> perfis = new ArrayList<>();

    public Conta() {
    }

    public static String erroDeSenha(String senha) {
        if (senha == null || senha.length() < 8)
            return "A senha deve ter no mínimo 8 caracteres";
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72)
            return "A senha deve ter no máximo 72 bytes";
        if (!senha.matches(".*[A-Za-z].*"))
            return "A senha deve conter ao menos uma letra";
        if (!senha.matches(".*\\d.*"))
            return "A senha deve conter ao menos um número";
        return null;
    }

    public static void gastarTempo(String senha) {
        CODIFICADOR.matches(senha == null ? "" : senha, HASH_FALSO);
    }

    public void definirSenha(String senhaPura) {
        hash = CODIFICADOR.encode(senhaPura);
    }

    public boolean senhaConfere(String senhaPura) {
        return hash != null && senhaPura != null && CODIFICADOR.matches(senhaPura, hash);
    }

    public String hashDaSenha() {
        return hash;
    }

    public void definirHash(String hash) {
        this.hash = hash;
    }

    public boolean existe() {
        return cta_cod > 0;
    }

    public boolean estaAtiva() {
        return ATIVA.equals(situacao);
    }

    public boolean estaPendente() {
        return PENDENTE.equals(situacao);
    }

    public boolean estaBloqueada(LocalDateTime agora) {
        return bloqueio_ate != null && agora.isBefore(bloqueio_ate);
    }

    public void registrarFalha(LocalDateTime agora) {
        tentativas++;
        if (tentativas >= MAX_TENTATIVAS) {
            bloqueio_ate = agora.plusMinutes(MINUTOS_DE_BLOQUEIO);
            tentativas = 0;
        }
    }

    public void zerarFalhas() {
        tentativas = 0;
        bloqueio_ate = null;
    }

    public boolean temFalhasRegistradas() {
        return tentativas > 0 || bloqueio_ate != null;
    }

    public boolean maiorDeIdade(LocalDate hoje) {
        return membro != null && membro.getDt_nascto() != null
                && !membro.getDt_nascto().plusYears(IDADE_MINIMA).isAfter(hoje);
    }

    public void calcularPerfis(Conexao conexao) {
        perfis = new ArrayList<>();
        if (admin)
            perfis.add(UsuarioLogado.ADMIN);
        for (String perfil : new ContaDAO().perfisDoMembro(conexao, membro.getMem_cod()))
            if (!perfis.contains(perfil))
                perfis.add(perfil);
    }

    public UsuarioLogado paraUsuario() {
        return new UsuarioLogado(membro.getMem_cod(), membro.getNome(), perfis);
    }

    public boolean gravar(Conexao conexao) {
        return new ContaDAO().gravar(conexao, this);
    }

    public boolean alterar(Conexao conexao) {
        return new ContaDAO().alterar(conexao, this);
    }

    public Conta buscarPorCpf(Conexao conexao, String cpf) {
        return new ContaDAO().doCpf(conexao, cpf);
    }

    public Conta buscarPorMembro(Conexao conexao, int memCod) {
        return new ContaDAO().doMembro(conexao, memCod);
    }

    public Conta buscarPorCodigo(Conexao conexao, int ctaCod) {
        return new ContaDAO().get(conexao, ctaCod);
    }

    public List<Conta> listar(Conexao conexao) {
        return new ContaDAO().contasExistentes(conexao);
    }

    public int contarOutrosAdministradoresAtivos(Conexao conexao) {
        return new ContaDAO().contarOutrosAdministradoresAtivos(conexao, cta_cod);
    }

    public Membro membro() {
        return membro;
    }

    public void definirMembro(Membro membro) {
        this.membro = membro;
    }

    public int getCta_cod() {
        return cta_cod;
    }

    public void setCta_cod(int cta_cod) {
        this.cta_cod = cta_cod;
    }

    public int getMem_cod() {
        return membro == null ? 0 : membro.getMem_cod();
    }

    public String getNome() {
        return membro == null ? null : membro.getNome();
    }

    public String getCpf() {
        return membro == null ? null : membro.getCPF();
    }

    public String getEmail() {
        return membro == null ? null : membro.getEmail();
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public int getTentativas() {
        return tentativas;
    }

    public void setTentativas(int tentativas) {
        this.tentativas = tentativas;
    }

    public LocalDateTime getBloqueio_ate() {
        return bloqueio_ate;
    }

    public void setBloqueio_ate(LocalDateTime bloqueio_ate) {
        this.bloqueio_ate = bloqueio_ate;
    }

    public LocalDate getDt_criacao() {
        return dt_criacao;
    }

    public void setDt_criacao(LocalDate dt_criacao) {
        this.dt_criacao = dt_criacao;
    }

    public List<String> getPerfis() {
        return perfis;
    }
}
