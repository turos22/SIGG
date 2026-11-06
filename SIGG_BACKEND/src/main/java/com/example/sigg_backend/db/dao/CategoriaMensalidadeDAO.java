package com.example.sigg_backend.db.dao;

import com.example.sigg_backend.db.util.IDAO;
import com.example.sigg_backend.db.util.SingletonDB;
import com.example.sigg_backend.models.CategoriaMensalidade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaMensalidadeDAO implements IDAO<CategoriaMensalidade> {

    @Override
    public boolean gravar(CategoriaMensalidade categoria){
        throw new UnsupportedOperationException("Gravação de categoria de mensalidade não implementada");
    }

    @Override
    public boolean alterar(CategoriaMensalidade categoria){
        throw new UnsupportedOperationException("Alteração de categoria de mensalidade não implementada");
    }

    @Override
    public boolean apagar(CategoriaMensalidade categoria){
        throw new UnsupportedOperationException("Exclusão de categoria de mensalidade não implementada");
    }

    @Override
    public List<CategoriaMensalidade> get(){
        return buscar("");
    }

    @Override
    public List<CategoriaMensalidade> get(String filtro){
        throw new UnsupportedOperationException("Filtro de categoria de mensalidade não implementado");
    }

    @Override
    public CategoriaMensalidade get(int id){
        List<CategoriaMensalidade> categorias = buscar("WHERE c.cat_mens_cod = ?", id);
        return categorias.isEmpty() ? null : categorias.get(0);
    }

    private List<CategoriaMensalidade> buscar(String condicao, Object... valores){
        ResultSet rs = SingletonDB.getConexao().consultar(
                "SELECT c.cat_mens_cod, c.cat_mens_valor, c.cat_mens_qtd_irmao FROM categoria_mensalidade c "
                        + condicao + " ORDER BY c.cat_mens_cod", valores);
        List<CategoriaMensalidade> categorias = new ArrayList<>();
        try {
            while (rs != null && rs.next()) {
                CategoriaMensalidade categoria = new CategoriaMensalidade();
                categoria.setCat_mens_cod(rs.getInt("cat_mens_cod"));
                categoria.setValor(BigDecimal.valueOf(rs.getDouble("cat_mens_valor")).setScale(2, RoundingMode.HALF_UP));
                categoria.setQtd_irmao(rs.getInt("cat_mens_qtd_irmao"));
                categorias.add(categoria);
            }
        } catch (SQLException sqlex) {
            categorias.clear();
        }
        return categorias;
    }
}
