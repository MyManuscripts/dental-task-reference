
package org.example.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PracticeDao {

    /**
     * Информация о филиале (practice_location).
     * Используется для заполнения настроек справки (ИНН/КПП/название).
     */
    public static class PracticeInfo {

        public final int id;
        public final String name;
        public final String inn;
        public final String kpp;

        public PracticeInfo(int id, String name, String inn, String kpp) {
            this.id = id;
            this.name = name != null ? name.trim() : "";
            this.inn = inn != null ? inn.trim() : "";
            this.kpp = kpp != null ? kpp.trim() : "";
        }
    }

    /**
     * Загружает список филиалов с ИНН и КПП из dba.practice_locations.
     * Фильтрует пустые/нулевые описания.
     *
     * @return список PracticeInfo, отсортированный по названию
     * @throws SQLException при ошибке подключения к БД
     */
    public List<PracticeInfo> loadPracticeInfoList() throws SQLException {
        String sql = """
        SELECT 
            practice_id AS id,
            description AS name,
            tax_file_no AS inn,
            medicare_prov_no AS kpp
        FROM dba.practice_locations
        WHERE description IS NOT NULL
          AND TRIM(description) != ''
        ORDER BY name
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<PracticeInfo> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new PracticeInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("inn"),
                        rs.getString("kpp")
                ));
            }
            return list;
        }
    }

    }



