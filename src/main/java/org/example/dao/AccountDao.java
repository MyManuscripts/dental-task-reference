package org.example.dao;

import org.example.model.MedicalAccount;
import org.example.model.Patient;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
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
              AND description NOT IN ('Финансы', 'Устаревшие', 'Сертификаты')
            ORDER BY description
            """;

        List<String> categories = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(rs.getString("description"));
            }
        }
        return categories;
    }


    public List<MedicalAccount> findAccountsForTaxReport(
            int practiceId,
            LocalDate startDate,
            LocalDate endDate,
            Integer patientId
    ) throws SQLException {


        String sql = """
        SELECT
            pa.id,
            pa.number,
            pa.date_created,
            pa.total,
            COALESCE(pa.rebate, 0) AS rebate,
            pa.amount_paid,
            p.surname,
            p.firstname,
            p.middlename,
            p.dob,
            p.itn AS inn,
            'Без категории' AS category
        FROM dba.patients_accounts pa
        JOIN dba.patients p ON pa.send_acc_to_pat_id = p.patient_id
        WHERE pa.amount_paid > 0
          AND pa.date_created >= ?
          AND pa.date_created <= ?
        """;

        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(startDate));
        params.add(Date.valueOf(endDate));

        // Фильтр по филиалу — только если practiceId > 0
        if (practiceId > 0) {
            sql += " AND pa.practice_id = ?";
            params.add(practiceId);
        }

        // Фильтр по пациенту — опционально
        if (patientId != null) {
            sql += " AND pa.send_acc_to_pat_id = ?";
            params.add(patientId);
        }

        // Иначе найденные платежи без категории
        sql += " ORDER BY pa.date_created DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            List<MedicalAccount> accounts = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MedicalAccount acc = new MedicalAccount();
                    acc.setId(rs.getInt("id"));
                    acc.setNumber(rs.getString("number"));
                    acc.setDateCreated(rs.getDate("date_created").toLocalDate());
                    acc.setTotal(rs.getBigDecimal("total"));
                    acc.setRebate(rs.getBigDecimal("rebate"));
                    acc.setAmountPaid(rs.getBigDecimal("amount_paid"));
                    acc.setSurname(rs.getString("surname"));
                    acc.setFirstname(rs.getString("firstname"));
                    acc.setMiddlename(rs.getString("middlename"));
                    acc.setBirthDate(rs.getDate("dob") != null ? rs.getDate("dob").toLocalDate() : null);
                    acc.setInn(rs.getString("inn"));
                    acc.setCategory("Без категории");
                    accounts.add(acc);
                }
            }
            return accounts;
        }
    }

    /**
     * Поиск пациентов по ФИО или номеру карты.
     * ВНИМАНИЕ: patients_cart_num — строка, ищем точное совпадение.
     */
    public List<Patient> findPatientsByQuery(int practiceId, String query) throws SQLException {
        String sql = """
            SELECT DISTINCT
                p.patient_id,
                p.surname,
                p.firstname,
                p.middlename,
                p.dob,
                p.itn,
                p.patients_cart_num
            FROM dba.patients p
            JOIN dba.patients_accounts pa ON pa.send_acc_to_pat_id = p.patient_id
            WHERE
                p.patients_cart_num = ?
                OR p.surname LIKE ?
                OR p.firstname LIKE ?
                OR p.middlename LIKE ?
            """;

        List<Object> params = new ArrayList<>();
        String likeQuery = "%" + query.trim() + "%";
        params.add(query.trim()); // точное совпадение по номеру карты
        params.add(likeQuery);
        params.add(likeQuery);
        params.add(likeQuery);

        if (practiceId > 0) {
            sql += " AND pa.practice_id = ?";
            params.add(practiceId);
        }

        sql += " ORDER BY p.surname, p.firstname";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            List<Patient> patients = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Patient p = new Patient();
                    p.setId(rs.getInt("patient_id"));
                    p.setSurname(rs.getString("surname"));
                    p.setFirstname(rs.getString("firstname"));
                    p.setMiddlename(rs.getString("middlename"));
                    p.setBirthDate(rs.getDate("dob") != null ? rs.getDate("dob").toLocalDate() : null);
                    p.setInn(rs.getString("itn"));
                    p.setCardNumber(rs.getString("patients_cart_num"));
                    patients.add(p);
                }
            }
            return patients;
        }
    }
}