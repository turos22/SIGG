package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.models.Membro;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MembroDAO implements IDAO<Membro> {
    @Override
    public boolean gravar(Conexao conexao, Membro membro){
        int ultimo = conexao.getMaxPK("membro", "mem_cod");
        if (ultimo < 0)
            return false;
        membro.setMem_cod(ultimo + 1);
        if (membro.getDt_ini() == null)
            membro.setDt_ini(LocalDate.now());
        return conexao.manipular("INSERT INTO membro (mem_cod, mem_nome, mem_dt_nascto, mem_cpf, mem_rg, mem_cep, mem_end,"
                        + " mem_end_num, mem_end_bairro, mem_cidade, mem_estado, mem_tel, mem_email, mem_dt_ini, mem_dt_fim)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                membro.getMem_cod(), membro.getNome(), membro.getDt_nascto(), membro.getCPF(), membro.getRG(),
                membro.getCEP(), membro.getEndereco(), membro.getEndereco_num(), membro.getEndereco_bairro(),
                membro.getCidade(), membro.getEstado(), membro.getTelefone(), membro.getEmail(),
                membro.getDt_ini(), membro.getDt_fim());
    }
    @Override
    public boolean alterar(Conexao conexao, Membro membro){
        return conexao.manipular("UPDATE membro SET mem_nome = ?, mem_dt_nascto = ?, mem_cpf = ?, mem_rg = ?,"
                        + " mem_cep = ?, mem_end = ?, mem_end_num = ?, mem_end_bairro = ?, mem_cidade = ?, mem_estado = ?,"
                        + " mem_tel = ?, mem_email = ?, mem_dt_ini = ?, mem_dt_fim = ? WHERE mem_cod = ?",
                membro.getNome(), membro.getDt_nascto(), membro.getCPF(), membro.getRG(), membro.getCEP(),
                membro.getEndereco(), membro.getEndereco_num(), membro.getEndereco_bairro(), membro.getCidade(),
                membro.getEstado(), membro.getTelefone(), membro.getEmail(), membro.getDt_ini(), membro.getDt_fim(),
                membro.getMem_cod());
    }

    @Override
    public boolean apagar(Conexao conexao, Membro membro){
        return conexao.manipular(
                "UPDATE membro SET mem_dt_fim = CURRENT_DATE WHERE mem_cod = ?", membro.getMem_cod());
    }
    @Override
    public List<Membro> get(Conexao conexao){
        return buscar(conexao, "");
    }
    @Override
    public List<Membro> get(Conexao conexao, String filtro){
        return buscar(conexao, "AND m.mem_nome ILIKE ?", "%" + filtro + "%");
    }
    @Override
    public Membro get(Conexao conexao, int id){
        List<Membro> membros = buscar(conexao, "AND m.mem_cod = ?", id);
        return membros.isEmpty() ? null : membros.get(0);
    }


    public boolean existeCpf(Conexao conexao, String cpf){
        ResultSet rs = conexao.consultar("SELECT 1 FROM membro WHERE mem_cpf = ?", cpf);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }


    public boolean existeCpfDeOutro(Conexao conexao, String cpf, int memCod){
        ResultSet rs = conexao.consultar("SELECT 1 FROM membro WHERE mem_cpf = ? AND mem_cod <> ?", cpf, memCod);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
    }

    public List<Membro> listarComPapeis(Conexao conexao, boolean somenteVoluntarios){
        return buscarComPapeis(conexao, somenteVoluntarios ? "AND " + VOLUNTARIO : "");
    }

    public Membro getComPapeis(Conexao conexao, int memCod){
        List<Membro> membros = buscarComPapeis(conexao, "AND m.mem_cod = ?", memCod);
        return membros.isEmpty() ? null : membros.get(0);
    }

    public Membro preencher(ResultSet rs) throws SQLException {
        Membro membro = new Membro();
        membro.setMem_cod(rs.getInt("mem_cod"));
        membro.setNome(rs.getString("mem_nome"));
        membro.setDt_nascto(Datas.ler(rs, "mem_dt_nascto"));
        membro.setCPF(rs.getString("mem_cpf"));
        membro.setRG(rs.getString("mem_rg"));
        membro.setCEP(rs.getString("mem_cep"));
        membro.setEndereco(rs.getString("mem_end"));
        membro.setEndereco_num(rs.getInt("mem_end_num"));
        membro.setEndereco_bairro(rs.getString("mem_end_bairro"));
        membro.setCidade(rs.getString("mem_cidade"));
        membro.setEstado(rs.getString("mem_estado"));
        membro.setTelefone(rs.getString("mem_tel"));
        membro.setEmail(rs.getString("mem_email"));
        membro.setDt_ini(Datas.ler(rs, "mem_dt_ini"));
        membro.setDt_fim(Datas.ler(rs, "mem_dt_fim"));
        return membro;
    }


    private List<Membro> buscar(Conexao conexao, String condicao, Object... valores) {
        ResultSet rs = conexao.consultar(
                "SELECT m.* FROM membro m WHERE m.mem_dt_fim IS NULL " + condicao + " ORDER BY m.mem_nome", valores);
        List<Membro> membros = new ArrayList<>();
        try {
            while (rs != null && rs.next())
                membros.add(preencher(rs));
        } catch (SQLException sqlex) {
            membros.clear();
        }
        return membros;
    }

    private static final String VOLUNTARIO = "m.mem_dt_nascto <= CURRENT_DATE - INTERVAL '18 years'";

    // Papéis vigentes: presidente com gestão cobrindo hoje, chefe sem gestão encerrada (mesmo critério do login).
    // Voluntário não é tabela: é qualquer pessoa com 18 anos ou mais.
    private List<Membro> buscarComPapeis(Conexao conexao, String condicao, Object... valores){
        ResultSet rs = conexao.consultar(
                "SELECT m.*,"
                        + " EXISTS (SELECT 1 FROM presidente x WHERE x.mem_cod = m.mem_cod"
                        + "   AND x.pres_gestao_ini <= CURRENT_DATE AND x.pres_gestao_fim >= CURRENT_DATE) AS eh_presidente,"
                        + " EXISTS (SELECT 1 FROM chefe x WHERE x.mem_cod = m.mem_cod"
                        + "   AND (x.chefe_gestao_fim IS NULL OR x.chefe_gestao_fim >= CURRENT_DATE)) AS eh_chefe,"
                        + " EXISTS (SELECT 1 FROM financeiro x WHERE x.mem_cod = m.mem_cod) AS eh_financeiro,"
                        + " EXISTS (SELECT 1 FROM escoteiro x WHERE x.mem_cod = m.mem_cod) AS eh_escoteiro,"
                        + " EXISTS (SELECT 1 FROM responsável x WHERE x.mem_cod = m.mem_cod) AS eh_responsavel,"
                        + " (" + VOLUNTARIO + ") AS eh_voluntario"
                        + " FROM membro m WHERE m.mem_dt_fim IS NULL " + condicao + " ORDER BY m.mem_nome", valores);
        List<Membro> membros = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                Membro membro = preencher(rs);
                List<String> papeis = new ArrayList<>();
                if (rs.getBoolean("eh_presidente")) papeis.add("Presidente");
                if (rs.getBoolean("eh_chefe")) papeis.add("Chefe");
                if (rs.getBoolean("eh_financeiro")) papeis.add("Financeiro");
                if (rs.getBoolean("eh_escoteiro")) papeis.add("Escoteiro");
                if (rs.getBoolean("eh_responsavel")) papeis.add("Responsável");
                if (rs.getBoolean("eh_voluntario")) papeis.add("Voluntário");
                membro.setPapeis(papeis);
                membros.add(membro);
            }
        } catch (SQLException sqlex) {
            membros.clear();
        }
        return membros;
    }
}
