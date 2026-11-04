package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.Conexao;
import com.example.sigg_backend.db.util.Datas;
import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.Membro;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MembroDAO implements IDAO<Membro> {
    @Override
    public boolean gravar(Membro membro){
        Conexao conexao = SingletonDB.getConexao();
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
    public boolean alterar(Membro membro){
        return SingletonDB.getConexao().manipular("UPDATE membro SET mem_nome = ?, mem_dt_nascto = ?, mem_cpf = ?, mem_rg = ?,"
                        + " mem_cep = ?, mem_end = ?, mem_end_num = ?, mem_end_bairro = ?, mem_cidade = ?, mem_estado = ?,"
                        + " mem_tel = ?, mem_email = ?, mem_dt_ini = ?, mem_dt_fim = ? WHERE mem_cod = ?",
                membro.getNome(), membro.getDt_nascto(), membro.getCPF(), membro.getRG(), membro.getCEP(),
                membro.getEndereco(), membro.getEndereco_num(), membro.getEndereco_bairro(), membro.getCidade(),
                membro.getEstado(), membro.getTelefone(), membro.getEmail(), membro.getDt_ini(), membro.getDt_fim(),
                membro.getMem_cod());
    }

    @Override
    public boolean apagar(Membro membro){
        return SingletonDB.getConexao().manipular(
                "UPDATE membro SET mem_dt_fim = CURRENT_DATE WHERE mem_cod = ?", membro.getMem_cod());
    }
    @Override
    public List<Membro> get(){
        return buscar("");
    }
    @Override
    public List<Membro> get(String filtro){
        return buscar("AND m.mem_nome ILIKE ?", "%" + filtro + "%");
    }
    @Override
    public Membro get(int id){
        List<Membro> membros = buscar("AND m.mem_cod = ?", id);
        return membros.isEmpty() ? null : membros.get(0);
    }


    public boolean existeCpf(String cpf){
        ResultSet rs = SingletonDB.getConexao().consultar("SELECT 1 FROM membro WHERE mem_cpf = ?", cpf);
        try {
            return rs != null && rs.next();
        } catch (SQLException sqlex) {
            return false;
        }
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


    private List<Membro> buscar(String condicao, Object... valores) {
        ResultSet rs = SingletonDB.getConexao().consultar(
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
}
