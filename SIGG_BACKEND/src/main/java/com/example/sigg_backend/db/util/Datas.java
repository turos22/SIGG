package com.example.sigg_backend.db.util;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Datas {

    private Datas() {
    }

    public static LocalDate ler(ResultSet rs, String coluna) throws SQLException {
        Date data = rs.getDate(coluna);
        return data == null ? null : data.toLocalDate();
    }
}
