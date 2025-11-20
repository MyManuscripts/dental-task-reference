
package org.example.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PracticeDao {

    public List<String> loadAllPractices() throws SQLException {

        String sql = """
            SELECT DISTINCT practice_id AS id
            FROM dba.patients
            WHERE practice_id IS NOT NULL AND practice_id > 0
            ORDER BY practice_id
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<String> practices = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                practices.add("Филиал " + id);
            }
            return practices;
        }
    }
}