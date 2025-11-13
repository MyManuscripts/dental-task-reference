
package org.example.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.AccountDao;
import org.example.model.MedicalAccount;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaxReferenceView {
    private final AccountDao accountDao = new AccountDao();

    // UI компоненты
    private final ComboBox<String> categoryComboBox = new ComboBox<>();
    private final Button loadCategoriesButton = new Button("Загрузить категории");
    private final Button loadAccountsButton = new Button("Загрузить счета");
    private final TableView<MedicalAccount> accountsTable = new TableView<>();
    private final Label statusLabel = new Label();

    private final ObservableList<MedicalAccount> accountsData = FXCollections.observableArrayList();

    public Scene getScene() {
        // Настройка таблицы
        setupAccountsTable();

        // Компоновка
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(
                new Label("Dental Tax Reference — Формирование справки ФНС"),
                loadCategoriesButton,
                new Label("Выберите категорию:"),
                categoryComboBox,
                loadAccountsButton,
                new Label("Счета пациентов:"),
                accountsTable,
                statusLabel
        );

        // Обработчики событий
        loadCategoriesButton.setOnAction(e -> loadCategories());
        loadAccountsButton.setOnAction(e -> loadAccounts());

        return new Scene(root, 800, 600);
    }

    private void setupAccountsTable() {
        // Колонки таблицы
        TableColumn<MedicalAccount, String> numberCol = new TableColumn<>("Номер");
        numberCol.setCellValueFactory(cell -> cell.getValue().numberProperty());

        TableColumn<MedicalAccount, String> patientCol = new TableColumn<>("Пациент");
        patientCol.setCellValueFactory(cell ->
                cell.getValue().surnameProperty()
                        .concat(" ")
                        .concat(cell.getValue().firstnameProperty())
        );

        TableColumn<MedicalAccount, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(cell ->
                cell.getValue().dateCreatedProperty()
                        .asString("%1$tY-%1$tm-%1$td")
        );

        TableColumn<MedicalAccount, String> totalCol = new TableColumn<>("Сумма");
        totalCol.setCellValueFactory(cell ->
                cell.getValue().totalProperty().asString("%.2f")
        );

        TableColumn<MedicalAccount, String> categoryCol = new TableColumn<>("Категория");
        categoryCol.setCellValueFactory(cell -> cell.getValue().categoryProperty());

        accountsTable.getColumns().addAll(numberCol, patientCol, dateCol, totalCol, categoryCol);
        accountsTable.setItems(accountsData);
    }

    private void loadCategories() {
        new Thread(() -> {
            try {
                List<String> categories = accountDao.loadAllCategories();
                // Обновляем UI в JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    categoryComboBox.setItems(FXCollections.observableArrayList(categories));
                    statusLabel.setText("✅ Загружено " + categories.size() + " категорий");
                    statusLabel.setText("✅ Загружено " + categories.size() + " категорий");
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() ->
                        statusLabel.setText("❌ Ошибка загрузки категорий: " + e.getMessage())
                );
            }
        }).start();
    }

    private void loadAccounts() {
        String selectedCategory = categoryComboBox.getValue();
        if (selectedCategory == null) {
            statusLabel.setText("⚠️ Выберите категорию");
            return;
        }

        new Thread(() -> {
            try {
                List<MedicalAccount> accounts = accountDao.findAccountsForTaxReport(
                        1,
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 12, 31),
                        List.of(selectedCategory)  // одна выбранная категория
                );
                javafx.application.Platform.runLater(() -> {
                    accountsData.setAll(accounts);
                    statusLabel.setText("✅ Найдено " + accounts.size() + " счетов");
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() ->
                        statusLabel.setText("❌ Ошибка загрузки счетов: " + e.getMessage())
                );
            }
        }).start();
    }
}