package org.example.dao;

import org.example.model.MedicalAccount;
import org.example.model.Patient;

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


            stmt.setInt(1, practiceId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));


            for (int i = 0; i < selectedCategories.size(); i++) {
                stmt.setString(4 + i, selectedCategories.get(i));
            }



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
                    acc.setCategory(rs.getString("category")); // ← после setInn(...)
                    accounts.add(acc);



                }
            }return accounts;

        }
    }

    /**
     * Ищет пациентов по ФИО или ID карты в указанном филиале.
     * @param practiceId 0 = все филиалы, иначе конкретный ID
     * @param query часть ФИО или номер карты
     */
    /**
     * Ищет пациентов по ФИО или ID карты в указанном филиале.
     * @param practiceId 0 = все филиалы, иначе конкретный ID
     * @param query часть ФИО или номер карты
     */
    public List<Patient> findPatientsByQuery(int practiceId, String query) throws SQLException {
        StringBuilder sql = new StringBuilder("""
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
        WHERE p.surname LIKE ? OR 
        p.firstname LIKE ? OR 
        p.middlename LIKE ? OR
        p."patients_cart_num" LIKE ?
        """);

        List<Object> params = new ArrayList<>();
        String likeQuery = "%" + query.trim() + "%";
        params.add(likeQuery);
        params.add(likeQuery);
        params.add(likeQuery);
        params.add(likeQuery);

        /*if (query.matches("\\d+")) { // если запрос — число
            sql.append(" OR p.patients_cart_num = ?"); // ← точное совпадение
            params.add(Integer.parseInt(query));  // ← передаём число
        } else {
            sql.append(" OR p.patients_cart_num LIKE ?"); // подстрока
            params.add("%" + query + "%");
        }*/

        if (practiceId > 0) {
            sql.append(" AND pa.practice_id = ?");
            params.add(practiceId);
        }

        sql.append(" ORDER BY p.surname, p.firstname");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

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
                    //p.setCardNumber(String.valueOf(p.getId()));
                    p.setCardNumber(rs.getString("patients_cart_num"));
                    patients.add(p);
                }
            }
            return patients;
        }
    }


}