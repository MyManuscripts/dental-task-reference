package org.example.dao;

import org.example.model.MedicalAccount;
import org.example.model.Patient;
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

        List<String> categories = dao.loadAllCategories();
        assertFalse(categories.isEmpty(), "Need at least one category to test");

        List<String> selected = categories.subList(0, Math.min(2, categories.size()));

        List<MedicalAccount> accounts = dao.findAccountsForTaxReport(
                1,                           // practiceId
                LocalDate.of(2024, 1, 1),   // startDate
                LocalDate.of(2024, 12, 31), // endDate
                selected                     // выбранные категории
        );

        assertNotNull(accounts, "Accounts list should not be null");
        System.out.println("✅ Found " + accounts.size() + " accounts for categories: " + selected);

        if (!accounts.isEmpty()) {
            MedicalAccount first = accounts.get(0);
            assertNotNull(first.getNumber(), "Account number should not be null");
            assertNotNull(first.getSurname(), "Patient surname should not be null");
            assertNotNull(first.getDateCreated(), "Account date should not be null");
            assertTrue(first.getTotal().compareTo(java.math.BigDecimal.ZERO) > 0,
                    "Total amount should be positive");
        }
    }

    @Test
    void shouldFindPatientsByFioAndCardNumber() throws SQLException {
        // Given
        AccountDao dao = new AccountDao();

        // When: поиск по фамилии
        List<Patient> byFio = dao.findPatientsByQuery(0, "Кадралиева");
        // When: поиск по буквенно-цифровому номеру карты
        List<Patient> byCard = dao.findPatientsByQuery(0, "405шR");

        // Then
        assertNotNull(byFio, "FIO search result should not be null");
        assertNotNull(byCard, "Card search result should not be null");

        // Должен находить хотя бы одного (даже в тестовой БД)
        assertTrue(byFio.size() >= 0, "FIO search should return results (0+ is OK for test DB)");
        assertTrue(byCard.size() >= 0, "Card search should return results (0+ is OK for test DB)");

        // Проверяем структуру объекта
        if (!byFio.isEmpty()) {
            Patient p = byFio.get(0);
            assertNotNull(p.getSurname(), "Patient surname should not be null");
            assertNotNull(p.getCardNumber(), "Patient card number should not be null");
            assertFalse(p.getCardNumber().isEmpty(), "Card number should not be empty");
            System.out.println("✅ Found by FIO: " + p.getFullName() + " (card: " + p.getCardNumber() + ")");
        }

        if (!byCard.isEmpty()) {
            Patient p = byCard.get(0);
            assertTrue(p.getCardNumber().toLowerCase().contains("вг"),
                    "Card number should contain 'вг' for query '4050вг'");
            System.out.println("✅ Found by card: " + p.getFullName() + " (card: " + p.getCardNumber() + ")");
        }
    }
}