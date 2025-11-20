package org.example.dao;

import org.example.model.TaxReferenceSettings;

import java.sql.*;
import java.util.*;

/**
 * Менеджер категорий процедур для справки ФНС.
 * Отвечает за загрузку, обновление и синхронизацию списка категорий с БД.
 */
public class CategoryManager {

    /**
     * Загружает ВСЕ доступные категории процедур из БД.
     * Исключает служебные категории: Финансы, Устаревшие, Сертификаты.
     *
     * @return упорядоченное множество уникальных названий категорий
     * @throws SQLException при ошибке подключения или выполнения запроса
     */
    public static Set<String> loadAllCategories() throws SQLException {
        String sql = """
            SELECT DISTINCT description
            FROM dba.general_procedures_lev_2
            WHERE level_1_id = 0
              AND description NOT IN ('Финансы', 'Устаревшие', 'Сертификаты')
            ORDER BY description
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {


            Set<String> categories = new LinkedHashSet<>();
            while (rs.next()) {
                String description = rs.getString("description");
                if (description != null && !description.trim().isEmpty()) {
                    categories.add(description.trim());
                }
            }
            return categories;
        }
    }

    /**
     * Обновляет настройки: добавляет в selectedCategories все категории из БД,
     * которые ещё не выбраны (например, после добавления новых разделов в D4W).
     *
     * Сохраняет уже выбранные категории — не сбрасывает настройки!
     *
     * @param settings объект настроек
     * @throws SQLException при ошибке работы с БД
     */
    public static void updateSettingsWithNewCategories(TaxReferenceSettings settings) throws SQLException {
        Set<String> allCategories = loadAllCategories();
        Set<String> current = new HashSet<>(settings.getProcedureCategories()); // копия


        for (String category : allCategories) {
            if (!current.contains(category)) {
                settings.addProcedureCategory(category);
            }
        }
    }

    /**
     * Сбрасывает выбранное в настройках и устанавливает новые категории.
     * Используется после закрытия диалога "Выбор разделов..." при нажатии "Сохранить".
     *
     * @param settings объект настроек
     * @param selected новый набор выбранных категорий (может быть пустым)
     */
    public static void saveSelectedCategories(TaxReferenceSettings settings, Set<String> selected) {
        if (selected == null) {
            selected = new HashSet<>();
        }
        settings.setProcedureCategories(selected);
    }

    /**
     * Устанавливает ВСЕ категории по умолчанию (для сброса настроек).
     *
     * @param settings объект настроек
     * @throws SQLException при ошибке загрузки из БД
     */
    public static void selectAllCategories(TaxReferenceSettings settings) throws SQLException {
        Set<String> all = loadAllCategories();
        settings.setProcedureCategories(all);
    }
}