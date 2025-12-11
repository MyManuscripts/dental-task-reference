package org.example.dao;

import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционный тест для PracticeDao.
 * Требует запущенной БД Dental4Windows.
 * Проверяет: загрузку реальных филиалов с ИНН и КПП.
 */

class PracticeDaoTest {

    @Test
    void shouldLoadPracticeInfoWithInnAndKpp() throws SQLException {
        // Given
        PracticeDao dao = new PracticeDao();

        // When
        List<PracticeDao.PracticeInfo> practices = dao.loadPracticeInfoList();


        // Then
        assertNotNull(practices, "Список филиалов не должен быть null");
        assertFalse(practices.isEmpty(), "Список филиалов не должен быть пустым");

        // Проверяем, что у каждого филиала есть название и (возможно) ИНН/КПП
        for (PracticeDao.PracticeInfo p : practices) {
            assertNotNull(p.name, "Название филиала не должно быть null");
            assertFalse(p.name.trim().isEmpty(), "Название филиала не должно быть пустым");
            // ИНН/КПП могут быть пустыми — это нормально (например, если не заполнены в БД)
        }
        System.out.println("Загружено филиалов: " + practices.size());
        practices.forEach(p ->
                System.out.printf(" • %s | ИНН: '%s' | КПП: '%s'%n", p.name, p.inn, p.kpp));
    }
}