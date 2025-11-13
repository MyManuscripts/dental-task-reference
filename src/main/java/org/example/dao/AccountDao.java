package org.example.dao;

import org.example.model.MedicalAccount;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;



/**
 * DAO-класс для работы со счетами пациентов (patients_accounts)
 * и формирования данных для справки ФНС.
 */
public class AccountDao {
    /**
     * Загружает список всех доступных категорий процедур из БД.
     * Исключает служебные категории: "Финансы", "Устаревшие", "Сертификаты".
     *
     * @return список названий категорий (например, "Терапия", "Ортодонтия")
     * @throws SQLException если произошла ошибка при работе с БД
     */

    public List<String> loadAllCategories() throws SQLException {

        String sql = """
                SELECT DISTINCT description
                FROM dba.general_procedures_lev_2
                WHERE level_1_id = 0
                and description NOT IN ('Финансы', 'Устаревшие', 'Сертификаты')
                ORDER BY description
                """;

        List<String> categories = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                categories.add(rs.getString("description"));

            }return categories;
        }
    }

    /**
     * Находит счета пациентов для формирования справки ФНС.
     *
     * @param practiceId         ID клиники (из настроек)
     * @param startDate          Начало периода (например, 2024-01-01)
     * @param endDate            Конец периода (например, 2024-12-31)
     * @param selectedCategories Список выбранных категорий (например, ["Терапия", "Ортодонтия"])
     * @return список счетов, соответствующих критериям
     * @throws SQLException если произошла ошибка при работе с БД
     */
    public List<MedicalAccount> findAccountsForTaxReport(
            int practiceId,
            LocalDate startDate,
            LocalDate endDate,
            List<String> selectedCategories) throws SQLException {

        if (selectedCategories == null || selectedCategories.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder inClause = new StringBuilder();

        for (int i = 0; i < selectedCategories.size(); i++) {
            if (i > 0) {
                inClause.append(", ");
            }
            inClause.append("?");
        }

        String sql = """
                SELECT DISTINCT
                    pa.id,
                    pa.number,
                    pa.date_created,
                    pa.total,
                    pa.rebate,
                    pa.amount_paid,
                    p.surname,
                    p.firstname,
                    p.middlename,
                    p.dob,
                    p.itn,
                    gpl2.description AS category
                FROM dba.patients_accounts pa
                JOIN dba.patients p ON pa.send_acc_to_pat_id = p.patient_id
                JOIN dba.treat t ON pa.id = t.account_id
                JOIN dba.procedures prc ON t.service_id = prc.item_id
                JOIN dba.general_procedures_lev_2 gpl2 ON prc.level_2_id = gpl2.id
                WHERE
                    pa.practice_id = ?
                    AND pa.amount_paid > 0
                    AND pa.date_created >= ?
                    AND pa.date_created <= ?
                    AND gpl2.description IN (%s)
                ORDER BY pa.date_created
                """.formatted(inClause);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Устанавливаем параметры
            stmt.setInt(1, practiceId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            // Устанавливаем категории
            for (int i = 0; i < selectedCategories.size(); i++) {
                stmt.setString(4 + i, selectedCategories.get(i));
            }


            // Обрабатываем результат
            List<MedicalAccount> accounts = new ArrayList<>();

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MedicalAccount acc = new MedicalAccount();

                    acc.setId(rs.getInt("id"));
                    acc.setNumber(rs.getString("number"));
                    acc.setDateCreated(
                            rs.getDate("date_created") != null
                                    ? rs.getDate("date_created").toLocalDate()
                                    : null
                    );
                    acc.setTotal(rs.getBigDecimal("total"));
                    acc.setRebate(rs.getBigDecimal("rebate"));
                    acc.setAmountPaid(rs.getBigDecimal("amount_paid"));
                    acc.setSurname(rs.getString("surname"));
                    acc.setFirstname(rs.getString("firstname"));
                    acc.setMiddlename(rs.getString("middlename"));
                    acc.setBirthDate(rs.getDate("dob") != null
                            ? rs.getDate("dob").toLocalDate() : null);
                    acc.setInn(rs.getString("inn"));
                    accounts.add(acc);



                }
            }return accounts;

        }


    }
}