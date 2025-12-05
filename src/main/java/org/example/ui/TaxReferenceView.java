package org.example.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import org.example.dao.AccountDao;
import org.example.dao.PracticeDao;
import org.example.model.MedicalAccount;
import org.example.model.Patient;
import org.example.model.TaxReferenceSettings;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class TaxReferenceView {

    private final PracticeDao practiceDao = new PracticeDao();
    private final AccountDao accountDao = new AccountDao();
    private final TaxReferenceSettings settings = new TaxReferenceSettings();
    private final Label titleLabel = new Label("Справка для налоговой");
    private final Button settingsButton = new Button("Настройки справки");
    private final ComboBox<String> practiceComboBox = new ComboBox<>();
    private final DatePicker reportDatePicker = new DatePicker();
    private final ComboBox<Integer> yearComboBox = new ComboBox<>();
    private final TextField patientSearchField = new TextField();
    private final Button findPatientButton = new Button("Найти");
    private final Button clearButton = new Button("Очистить");
    private final Button showPaymentsButton = new Button("Показать платежи пациента");
    private final CheckBox patientIsPayerCheckBox = new CheckBox("Пациент является налогоплательщиком для данной справки");
    private final TableView<MedicalAccount> paymentsTable = new TableView<>();
    private final ObservableList<MedicalAccount> paymentsData = FXCollections.observableArrayList();
    private final Label statusLabel = new Label();
    private final Map<String, Integer> practiceMap = new HashMap<>();

    // Секция "Сведения о пациенте"
    private final TextField patientNumberField = new TextField();
    private final TextField patientSurnameField = new TextField();
    private final TextField patientFirstNameField = new TextField();
    private final TextField patientMiddleNameField = new TextField();
    private final TextField patientBirthDateField = new TextField();
    private final TextField patientInnField = new TextField();
    private final TableView<Patient> patientTable = new TableView<>();
    private final ObservableList<Patient> patientData = FXCollections.observableArrayList();


    public Scene getScene() {
        initUI();
        VBox root = buildLayout();
        Scene scene = new Scene(root, 1000, 700);
        return scene;
    }


    private void initUI() {

        patientTable.setItems(patientData);

        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        settingsButton.setOnAction(e -> {
            new SettingsDialog(settings, (Stage) paymentsTable.getScene().getWindow());
        });

        practiceComboBox.getItems().addAll("Все филиалы", "Филиал 1", "Филиал 2");
        practiceComboBox.setValue("Все филиалы");


        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear; y >= currentYear - 3; y--) {
            yearComboBox.getItems().add(y);
        }


        yearComboBox.setValue(currentYear);
        reportDatePicker.setValue(LocalDate.now());


        findPatientButton.setOnAction(e -> {
            String query = patientSearchField.getText().trim();
            if (query.isEmpty()) {
                statusLabel.setText("Введите ФИО или номер карты");
                return;
            }

            String selectedPractice = practiceComboBox.getValue();
            int practiceId = 0;
            if (selectedPractice != null && !"Все филиалы".equals(selectedPractice)) {
                practiceId = practiceMap.get(selectedPractice);
            }

            // Заворачиваем в final-переменные
            final int finalPracticeId = practiceId;
            final String finalQuery = query;

            new Thread(() -> {
                try {
                    // Используем final-копии — безопасно внутри лямбды
                    List<Patient> patients = accountDao.findPatientsByQuery(finalPracticeId, finalQuery);

                    javafx.application.Platform.runLater(() -> {
                        if (patients.isEmpty()) {
                            statusLabel.setText("Пациент не найден");
                            clearPatientInfo();
                        } else {
                            Patient selected = patients.get(0);
                            settings.setSelectedPatient(selected);
                            displayPatientInfo(selected);
                            statusLabel.setText("Найден: " + selected.getFullName());
                        }
                    });
                } catch (SQLException ex) {
                    javafx.application.Platform.runLater(() ->
                            statusLabel.setText("Ошибка поиска: " + ex.getMessage()));
                }
            }).start();
        });


        clearButton.setOnAction(e -> patientSearchField.clear());
        showPaymentsButton.setOnAction(e -> loadPayments());
        patientIsPayerCheckBox.setSelected(true);


        loadPractices();
    }

    private void loadPractices() {
        new Thread(() -> {
            try {
                List<String> practices = practiceDao.loadAllPractices();
                final List<String> finalPractices = new ArrayList<>(practices);
                javafx.application.Platform.runLater(() -> {
                    practiceComboBox.getItems().clear();
                    practiceComboBox.getItems().add("Все филиалы");
                    practiceComboBox.getItems().addAll(finalPractices);


                    practiceMap.clear();
                    practiceMap.put("Все филиалы", 0); // Все филиалы

                    practiceComboBox.setValue("Все филиалы");
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() ->
                        statusLabel.setText("Ошибка загрузки филиалов: " + e.getMessage())
                );
            }
        }).start();
    }

    private void displayPatientInfo(Patient patient) {

        patientData.clear();
        patientData.add(patient); // добавляем одного пациента
        statusLabel.setText(" Найден: " + patient.getFullName());

        // Заполняем поля в секции "Сведения о пациенте"
        patientNumberField.setText(patient.getCardNumber()); // № карты
        patientSurnameField.setText(patient.getSurname());   // Фамилия
        patientFirstNameField.setText(patient.getFirstname()); // Имя
        patientMiddleNameField.setText(patient.getMiddlename()); // Отчество
        patientBirthDateField.setText(
                patient.getBirthDate() != null ?
                        patient.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : ""
        );
        patientInnField.setText(patient.getInn()); // ИНН

        // Обновляем статус
        statusLabel.setText(" Найден пациент: " + patient.getFullName());
    }


    private VBox buildLayout() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        root.getChildren().addAll(
                createHeaderSection(), // ← замените createHeader()
                createSearchSection(),
                createPayerSection(),
                createPaymentsSection(),
                statusLabel
        );
        return root;
    }

    private VBox createHeaderSection() {

        Label titleLabel = new Label("Справка для налоговой");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(javafx.geometry.Pos.CENTER);

        HBox filterBox = new HBox(20);
        filterBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        filterBox.getChildren().addAll(
                new Label("Выбор филиала:"),
                practiceComboBox,
                new Region(), // ← пустое пространство для растяжения
                settingsButton
        );

        HBox.setMargin(settingsButton, new Insets(0, 0, 0, 280));

        HBox.setHgrow(practiceComboBox, Priority.ALWAYS);

        return new VBox(10, titleBox, filterBox);
    }


    private VBox createPayerSection() {
        VBox box = new VBox(10);
        box.setStyle("-fx-border-color: #ccc; -fx-border-width: 1px; -fx-padding: 10px;");
        Label title = new Label("Сведения о пациенте");
        title.setStyle("-fx-font-weight: bold;");

        patientTable.setPrefHeight(80);
        patientTable.setItems(patientData);

        TableColumn<Patient, String> cardCol = new TableColumn<>("№ карты");
        cardCol.setCellValueFactory(p -> p.getValue().cardNumberProperty());

        TableColumn<Patient, String> surnameCol = new TableColumn<>("Фамилия");
        surnameCol.setCellValueFactory(p -> p.getValue().surnameProperty());

        TableColumn<Patient, String> nameCol = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(p -> p.getValue().firstnameProperty());

        TableColumn<Patient, String> middlenameCol = new TableColumn<>("Отчество");
        middlenameCol.setCellValueFactory(p -> p.getValue().middlenameProperty());

        TableColumn<Patient, String> dobCol = new TableColumn<>("Год рождения");
        dobCol.setCellValueFactory(p ->
                p.getValue().birthDateProperty().asString("%1$tY")
        );

        TableColumn<Patient, String> innCol = new TableColumn<>("ИНН");
        innCol.setCellValueFactory(p -> p.getValue().innProperty());

        patientTable.getColumns().addAll(cardCol, surnameCol, nameCol, middlenameCol, dobCol, innCol);

        // При клике на строку — заполняем поля
        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                settings.setSelectedPatient(selected);
                statusLabel.setText("Выбран: " + selected.getFullName());
            }
        });

        box.getChildren().addAll(title, patientTable);
        return box;
    }

    private void clearPatientInfo() {
        patientNumberField.clear();
        patientSurnameField.clear();
        patientFirstNameField.clear();
        patientMiddleNameField.clear();
        patientBirthDateField.clear();
        patientInnField.clear();
        patientData.clear();
        settings.setSelectedPatient(null);
    }

    private VBox createPaymentsSection() {
        VBox box = new VBox(10);
        box.setStyle("-fx-border-color: #ccc; -fx-border-width: 1px; -fx-padding: 10px;");
        Label title = new Label("Платежи пациента");
        title.setStyle("-fx-font-weight: bold;");
        setupPaymentsTable();
        box.getChildren().addAll(title, paymentsTable);
        return box;
    }

    private void setupPaymentsTable() {
        TableColumn<MedicalAccount, Boolean> selectCol = new TableColumn<>("Включить");
        selectCol.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));

        TableColumn<MedicalAccount, String> numberCol = new TableColumn<>("№ счёта");
        numberCol.setCellValueFactory(cell -> cell.getValue().numberProperty());

        TableColumn<MedicalAccount, String> patientCol = new TableColumn<>("Пациент");
        patientCol.setCellValueFactory(cell ->
                cell.getValue().surnameProperty()
                        .concat(" ")
                        .concat(cell.getValue().firstnameProperty())
        );

        TableColumn<MedicalAccount, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(cell ->
                cell.getValue().dateCreatedProperty().asString("%1$tY-%1$tm-%1$td")
        );

        TableColumn<MedicalAccount, String> totalCol = new TableColumn<>("Сумма");
        totalCol.setCellValueFactory(cell ->
                cell.getValue().totalProperty().asString("%.2f")
        );

        TableColumn<MedicalAccount, String> paidCol = new TableColumn<>("Оплачено");
        paidCol.setCellValueFactory(cell ->
                cell.getValue().amountPaidProperty().asString("%.2f")
        );

        TableColumn<MedicalAccount, String> categoryCol = new TableColumn<>("Категория");
        categoryCol.setCellValueFactory(cell -> cell.getValue().categoryProperty());

        paymentsTable.getColumns().addAll(selectCol, numberCol, patientCol, dateCol, totalCol, paidCol);
        paymentsTable.setItems(paymentsData);
        paymentsTable.setPlaceholder(new Label("Нажмите «Показать платежи пациента»"));
    }

    private void loadPayments() {
        Patient selectedPatient = settings.getSelectedPatient();
        if (selectedPatient == null) {
            paymentsData.clear();
            paymentsTable.setPlaceholder(new Label("Сначала найдите пациента"));
            statusLabel.setText("");
            return;
        }

        int year = yearComboBox.getValue();

        new Thread(() -> {
            try {

                List<MedicalAccount> accounts = accountDao.findAccountsForTaxReport(
                        0, // все филиалы
                        LocalDate.of(year, 1, 1),
                        LocalDate.of(year, 12, 31),
                        selectedPatient.getId()
                );

                javafx.application.Platform.runLater(() -> {
                    paymentsData.setAll(accounts); // Загружаем данные в таблицу
                    if (accounts.isEmpty()) {
                        paymentsTable.setPlaceholder(new Label(" Найдено 0 оплаченных счетов за " + year + " г."));
                    } else {
                        paymentsTable.setPlaceholder(new Label(" Найдено " + accounts.size() + " оплаченных счетов за " + year + " г."));
                    }
                    // Очистим статус внизу окна
                    statusLabel.setText("");
                });

            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    paymentsData.clear();
                    paymentsTable.setPlaceholder(new Label(" Ошибка: " + e.getMessage()));
                    statusLabel.setText(""); // Очистим статус внизу окна
                });
            }
        }).start();

    }


    private VBox createSearchSection() {
        VBox box = new VBox(8);
        box.setStyle("-fx-border-color: #ccc; -fx-border-width: 1px; -fx-padding: 10px;");
        Label title = new Label("Поиск пациента");
        title.setStyle("-fx-font-weight: bold;");

        HBox searchRow = new HBox(10);
        searchRow.getChildren().addAll(
                new Label("ФИО или № карты:"),
                patientSearchField,
                findPatientButton,
                clearButton
        );

        HBox dateRow = new HBox(20);
        dateRow.getChildren().addAll(
                new Label("Дата справки:"), reportDatePicker,
                new Label("Год справки:"), yearComboBox,
                showPaymentsButton
        );

        box.getChildren().addAll(title, searchRow, dateRow);
        return box;
    }
}

