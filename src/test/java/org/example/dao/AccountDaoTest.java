package org.example.dao;

import org.example.model.MedicalAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountDaoTest {

    private AccountDao dao;

    @BeforeEach
    void setUp() {
        dao = new AccountDao();
    }

    @Test
    void shouldLoadCategoriesFromDatabase() throws SQLException {
        List<String> categories = dao.loadAllCategories();
        assertFalse(categories.isEmpty(), "Categories list should not be empty");
        System.out.println("✅ Loaded categories: " + categories);
    }

    @Test
    void shouldFindAccountsForTaxReport() throws SQLException {
        // Given: загружаем реальные категории из БД
        List<String> categories = dao.loadAllCategories();
        assertFalse(categories.isEmpty(), "Need at least one category to test");

        // Выбираем первые 1-2 категории для теста (чтобы запрос был быстрее)
        List<String> selected = categories.subList(0, Math.min(2, categories.size()));

        // When: ищем счета за 2024 год для клиники №1
        List<MedicalAccount> accounts = dao.findAccountsForTaxReport(
                1,                           // practiceId
                LocalDate.of(2024, 1, 1),   // startDate
                LocalDate.of(2024, 12, 31), // endDate
                selected                     // выбранные категории
        );

        // Then: проверяем результат
        assertNotNull(accounts, "Accounts list should not be null");
        System.out.println("✅ Found " + accounts.size() + " accounts for categories: " + selected);

        // Если в БД есть данные — список не пустой
        // Если пусто — это тоже ОК (тест пройдёт), но вы увидите 0
        // Для надёжности проверим структуру хотя бы одного объекта, если есть
        if (!accounts.isEmpty()) {
            MedicalAccount first = accounts.get(0);
            assertNotNull(first.getNumber(), "Account number should not be null");
            assertNotNull(first.getSurname(), "Patient surname should not be null");
            assertNotNull(first.getDateCreated(), "Account date should not be null");
            assertTrue(first.getTotal().compareTo(java.math.BigDecimal.ZERO) > 0,
                    "Total amount should be positive");
        }
    }
}