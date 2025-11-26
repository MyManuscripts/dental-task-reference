
package org.example.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PracticeDao {

    /**
     * Загружает список филиалов: название → ID
     * Исключает лаборатории (по названию).
     */

    public List<String> loadAllPractices() throws SQLException {
        String sql = """
            SELECT DISTINCT description AS name
            FROM dba.practice_locations
            WHERE description NOT LIKE '%Лаборатория%'
              AND description IS NOT NULL
              AND description != ''
            ORDER BY name
            """;

        List<String> practices = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("name").trim();
                if (!name.isEmpty()) {
                    practices.add(name);
                }
            }
        }
        return practices;
    }



}