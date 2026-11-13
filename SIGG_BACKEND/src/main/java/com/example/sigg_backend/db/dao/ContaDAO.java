package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.models.Conta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO implements IDAO<Conta> {

    @Override
    public boolean gravar(Conexao conexao, Conta conta){
        int ultimo = conexao.getMaxPK("conta", "cta_cod");
        if (ultimo < 0)
            return false;
        conta.setCta_cod(ultimo + 1);
        return conexao.manipular("INSERT INTO conta (cta_cod, mem_cod, cta_senha, cta_situacao, cta_admin)"
                        + " VALUES (?, ?, ?, ?, ?)",
                conta.getCta_cod(), conta.membro().getMem_cod(), conta.hashDaSenha(), conta.getSituacao(), conta.isAdmin());
    }

    @Override
    public boolean alterar(Conexao conexao, Conta conta){
        return conexao.manipular("UPDATE conta SET cta_senha = ?, cta_situacao = ?, cta_admin = ?,"
                        + " cta_tentativas = ?, cta_bloqueio_ate = ? WHERE cta_cod = ?",
                conta.hashDaSenha(), conta.getSituacao(), conta.isAdmin(), conta.getTentativas(),
                conta.getBloqueio_ate(), conta.getCta_cod());
    }

    @Override
    public boolean apagar(Conexao conexao, Conta conta){
        return conexao.manipular("UPDATE conta SET cta_situacao = ? WHERE cta_cod = ?",
                Conta.INATIVA, conta.getCta_cod());
    }

    @Override
    public List<Conta> get(Conexao conexao){
        return buscar(conexao, "");
    }

    @Override
    public List<Conta> get(Conexao conexao, String filtro){
        return buscar(conexao, "AND m.mem_nome ILIKE ?", "%" + filtro + "%");
    }

    @Override
    public Conta get(Conexao conexao, int id){
        return primeira(buscar(conexao, "AND c.cta_cod = ?", id));
    }

    public List<Conta> contasExistentes(Conexao conexao){
        return buscar(conexao, "AND c.cta_cod IS NOT NULL");
    }

    public Conta doCpf(Conexao conexao, String cpf){
        return primeira(buscar(conexao, "AND m.mem_cpf = ?", cpf));
    }

    public Conta doMembro(Conexao conexao, int memCod){
        return primeira(buscar(conexao, "AND m.mem_cod = ?", memCod));
    }

    public int contarOutrosAdministradoresAtivos(Conexao conexao, int ctaCod){
        ResultSet rs = conexao.consultar(
                "SELECT COUNT(*) AS total FROM conta c JOIN membro m ON m.mem_cod = c.mem_cod"
                        + " WHERE c.cta_admin AND c.cta_situacao = 'A' AND m.mem_dt_fim IS NULL AND c.cta_cod <> ?", ctaCod);
        try {
            return rs != null && rs.next() ? rs.getInt("total") : 0;
        } catch (SQLException sqlex) {
            return 0;
        }
    }

    public List<String> perfisDoMembro(Conexao conexao, int memCod){
        ResultSet rs = conexao.consultar(
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

    private List<Conta> buscar(Conexao conexao, String condicao, Object... valores){
        ResultSet rs = conexao.consultar(
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
