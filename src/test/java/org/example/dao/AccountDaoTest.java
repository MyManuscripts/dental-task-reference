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
}