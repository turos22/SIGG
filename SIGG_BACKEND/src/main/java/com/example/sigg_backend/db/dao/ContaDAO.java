package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.Conta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO implements IDAO<Conta> {

    @Override
    public boolean gravar(Conta conta){
        Conexao conexao = SingletonDB.getConexao();
        int ultimo = conexao.getMaxPK("conta", "cta_cod");
        if (ultimo < 0)
            return false;
        conta.setCta_cod(ultimo + 1);
        return conexao.manipular("INSERT INTO conta (cta_cod, mem_cod, cta_senha, cta_situacao, cta_admin)"
                        + " VALUES (?, ?, ?, ?, ?)",
                conta.getCta_cod(), conta.membro().getMem_cod(), conta.hashDaSenha(), conta.getSituacao(), conta.isAdmin());
    }

    @Override
    public boolean alterar(Conta conta){
        return SingletonDB.getConexao().manipular("UPDATE conta SET cta_senha = ?, cta_situacao = ?, cta_admin = ?,"
                        + " cta_tentativas = ?, cta_bloqueio_ate = ? WHERE cta_cod = ?",
                conta.hashDaSenha(), conta.getSituacao(), conta.isAdmin(), conta.getTentativas(),
                conta.getBloqueio_ate(), conta.getCta_cod());
    }

    @Override
    public boolean apagar(Conta conta){
        return SingletonDB.getConexao().manipular("UPDATE conta SET cta_situacao = ? WHERE cta_cod = ?",
                Conta.INATIVA, conta.getCta_cod());
    }

    @Override
    public List<Conta> get(){
        return buscar("");
    }

    @Override
    public List<Conta> get(String filtro){
        return buscar("AND m.mem_nome ILIKE ?", "%" + filtro + "%");
    }

    @Override
    public Conta get(int id){
        return primeira(buscar("AND c.cta_cod = ?", id));
    }

    public List<Conta> contasExistentes(){
        return buscar("AND c.cta_cod IS NOT NULL");
    }

    public Conta doCpf(String cpf){
        return primeira(buscar("AND m.mem_cpf = ?", cpf));
    }

    public Conta doMembro(int memCod){
        return primeira(buscar("AND m.mem_cod = ?", memCod));
    }

    public List<String> perfisDoMembro(int memCod){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT 'PRESIDENTE' AS perfil FROM presidente WHERE mem_cod = ?"
                        + " AND pres_gestao_ini <= CURRENT_DATE AND pres_gestao_fim >= CURRENT_DATE"
                        + " UNION SELECT 'CHEFE' FROM chefe WHERE mem_cod = ?"
                        + " AND (chefe_gestao_fim IS NULL OR chefe_gestao_fim >= CURRENT_DATE)"
                        + " UNION SELECT 'FINANCEIRO' FROM financeiro WHERE mem_cod = ?"
                        + " UNION SELECT 'RESPONSAVEL' FROM responsável WHERE mem_cod = ?"
                        + " UNION SELECT 'ESCOTEIRO' FROM escoteiro WHERE mem_cod = ?"
                        + " ORDER BY perfil", memCod, memCod, memCod, memCod, memCod);
        List<String> perfis = new ArrayList<>();
        try {
            while (rs != null && rs.next())
                perfis.add(rs.getString("perfil"));
        } catch (SQLException sqlex) {
            perfis.clear();
        }
        return perfis;
    }

    private Conta primeira(List<Conta> contas){
        return contas.isEmpty() ? null : contas.get(0);
    }

    private Conta preencher(ResultSet rs) throws SQLException {
        Conta conta = new Conta();
        conta.definirMembro(new MembroDAO().preencher(rs));
        conta.setCta_cod(rs.getInt("cta_cod"));
        conta.definirHash(rs.getString("cta_senha"));
        conta.setSituacao(rs.getString("cta_situacao"));
        conta.setAdmin(rs.getBoolean("cta_admin"));
        conta.setTentativas(rs.getInt("cta_tentativas"));
        Timestamp bloqueio = rs.getTimestamp("cta_bloqueio_ate");
        conta.setBloqueio_ate(bloqueio == null ? null : bloqueio.toLocalDateTime());
        conta.setDt_criacao(Datas.ler(rs, "cta_dt_criacao"));
        return conta;
    }

    private List<Conta> buscar(String condicao, Object... valores){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT m.*, c.cta_cod, c.cta_senha, c.cta_situacao, c.cta_admin, c.cta_tentativas,"
                        + " c.cta_bloqueio_ate, c.cta_dt_criacao"
                        + " FROM membro m LEFT JOIN conta c ON c.mem_cod = m.mem_cod"
                        + " WHERE m.mem_dt_fim IS NULL " + condicao + " ORDER BY m.mem_nome", valores);
        List<Conta> contas = new ArrayList<>();
        try {
            while (rs != null && rs.next())
                contas.add(preencher(rs));
        } catch (SQLException sqlex) {
            contas.clear();
        }
        return contas;
    }
}
