package org.example.dao;

import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PracticeDaoTest {

    @Test
    void shouldLoadAllPractices() throws SQLException {
        // Given
        PracticeDao dao = new PracticeDao();

        // When
        List<String> practices = dao.loadAllPractices();

        // Then
        assertNotNull(practices, "Practices list should not be null");
        assertFalse(practices.isEmpty(), "Practices list should not be empty");
        System.out.println("Loaded practices: " + practices);
    }
}