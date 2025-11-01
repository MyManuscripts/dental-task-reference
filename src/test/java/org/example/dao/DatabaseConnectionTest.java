package org.example.dao;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Интеграционный тест подключения к SQL Anywhere.
 * Требует запущенной БД Dental4Windows.
 */


public class DatabaseConnectionTest {
    @Test
    void shouldEstablishDatabaseConnection() throws Exception{
        // When
        Connection connection = new DatabaseConnection().getConnection();

        // Then
        assertNotNull(connection, "Connection should not be NULL");
        connection.close();
    }
}
