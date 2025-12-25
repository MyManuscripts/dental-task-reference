package org.example.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import org.example.dao.AccountDao;
import org.example.dao.PracticeDao;
import org.example.model.MedicalAccount;
import org.example.model.Patient;
import org.example.model.TaxReferenceSettings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
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


    // === Футер: кнопки и чекбоксы ===
    private final TextField refNumberField = new TextField();
    private final CheckBox paperCarrierCheckBox = new CheckBox("Бумажный носитель");
    private final CheckBox fileCheckBox = new CheckBox("Файл");
    private final Button previewButton = new Button("Просмотр");
    private final Button closeButton = new Button("Закрыть");
    private final Button exportButton = new Button("Выгрузить");
    private final Button printButton = new Button("Печать");

    private Scene scene;
    private Stage ownerStage;

    public void setOwnerStage(Stage stage) {
        this.ownerStage = stage;
    }
    public TaxReferenceView() {
        initUI();
    }
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
        if (scene == null) {
            VBox root = buildLayout();
            scene = new Scene(root, 1000, 700);
        }
        return scene;
    }

    private void initUI() {

        patientTable.setItems(patientData);

        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        settingsButton.setOnAction(e -> {
            Stage stage = (Stage) getScene().getWindow(); // ← используйте getScene() из TaxReferenceView
            new SettingsDialog(settings, stage);
        });

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

            int practiceId = 0;


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
                            statusLabel.setText("");
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


        exportButton.setOnAction(e -> statusLabel.setText("Выгрузка — в разработке"));
        printButton.setOnAction(e -> statusLabel.setText("Печать — в разработке"));

    }


    private void displayPatientInfo(Patient patient) {

        patientData.clear();
        patientData.add(patient); // добавляем одного пациента
        statusLabel.setText("");

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

    private HBox createFooterSection() {
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.BASELINE_LEFT); // ← КЛЮЧЕВОЕ: выравнивание по baseline
        footer.setPadding(new Insets(10, 0, 0, 0));

        // 1. Поле "№ справки"
        Label refNumLabel = new Label("№ справки:");
        TextField refNumberField = new TextField();
        refNumberField.setPrefColumnCount(10);
        refNumberField.setText(settings.getReferenceNumber());
        refNumberField.textProperty().addListener((obs, old, newVal) ->
                settings.setReferenceNumber(newVal)
        );

        // 2. Чекбоксы
        CheckBox paperBox = new CheckBox("Бумажный носитель");
        CheckBox fileBox = new CheckBox("Файл");

        paperBox.setSelected(true);
        fileBox.setSelected(false);

        // Взаимоисключающее поведение
        paperBox.setOnAction(e -> {
            if (paperBox.isSelected()) fileBox.setSelected(false);
        });
        fileBox.setOnAction(e -> {
            if (fileBox.isSelected()) paperBox.setSelected(false);
        });

        // 3. Кнопки
        Button previewButton = new Button("Просмотр");
        Button closeButton = new Button("Закрыть");
        Button exportButton = new Button("Выгрузить");
        Button printButton = new Button("Печать");

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.BASELINE_RIGHT);
        buttonBox.getChildren().addAll(previewButton, closeButton, exportButton, printButton);

        // === Сборка — всё в один HBox ===
        footer.getChildren().addAll(
                refNumLabel,
                refNumberField,
                new Label("   "), // маленький отступ
                paperBox,
                new Label("   "),
                fileBox,
                new Region(), // ← растягиваемое пространство
                buttonBox
        );
        HBox.setHgrow(new Region(), Priority.ALWAYS);

        // Обработчики кнопок
        previewButton.setOnAction(e -> {
            Patient patient = settings.getSelectedPatient();
            if (patient == null) {
                statusLabel.setText("⚠️ Сначала найдите пациента");
                return;
            }
            if (paymentsData.isEmpty()) {
                statusLabel.setText("⚠️ Нет платежей для просмотра");
                return;
            }
            // Передаём ownerStage
            new PreviewDialog(settings, patient, new ArrayList<>(paymentsData), ownerStage).show();
        });

        closeButton.setOnAction(e -> {
            // Например, закрыть приложение или сбросить
            Platform.exit();
        });

        exportButton.setOnAction(e -> statusLabel.setText("Выгрузка — в разработке"));
        printButton.setOnAction(e -> statusLabel.setText("Печать — в разработке"));



        return footer;
    }

    /**
     * Собирает основной UI: заголовок, поиск, данные пациента, платежи, футер.
     * Используется один раз при создании сцены.
     *
     * @return корневой VBox с полным интерфейсом
     */
    private VBox buildLayout() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        root.getChildren().addAll(
                createHeaderSection(),
                createSearchSection(),
                createPayerSection(),
                createPaymentsSection(),
                createFooterSection(),
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

                settingsButton
        );


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

        TableColumn<MedicalAccount, String> doctorCol = new TableColumn<>("Врач");
        doctorCol.setCellValueFactory(cell -> cell.getValue().doctorNameProperty());

        TableColumn<MedicalAccount, String> numberCol = new TableColumn<>("№ счёта");
        numberCol.setCellValueFactory(cell -> cell.getValue().numberProperty());

        TableColumn<MedicalAccount, String> accountDateCol = new TableColumn<>("Дата счёта");
        accountDateCol.setCellValueFactory(cell ->
                cell.getValue().dateCreatedProperty().asString("%1$tY-%1$tm-%1$td")
        );


        TableColumn<MedicalAccount, String> totalCol = new TableColumn<>("Сумма");
        totalCol.setCellValueFactory(cell ->
                cell.getValue().totalProperty().asString("%.2f")
        );

        TableColumn<MedicalAccount, String> discountCol = new TableColumn<>("Скидка");
        discountCol.setCellValueFactory(cell ->
                cell.getValue().rebateProperty().asString("%.2f")
        );

        TableColumn<MedicalAccount, String> paidCol = new TableColumn<>("Оплачено");
        paidCol.setCellValueFactory(cell ->
                cell.getValue().amountPaidProperty().asString("%.2f")
        );

        TableColumn<MedicalAccount, String> payDateCol = new TableColumn<>("Дата оплаты");
        payDateCol.setCellValueFactory(cell ->
                cell.getValue().paymentDateProperty()
                        .asString("%1$tY-%1$tm-%1$td")
                        .orElse("")
        );


        paymentsTable.getColumns().setAll(
                selectCol, doctorCol, numberCol, accountDateCol,
                totalCol, discountCol, paidCol, payDateCol
        );
        paymentsTable.setItems(paymentsData);
        paymentsTable.setPlaceholder(new Label("Нажмите «Показать платежи пациента»"));
    }
    /**
     * Загружает платежи выбранного пациента за указанный год.
     * Использует {AccountDao#findAccountsForTaxReport} с фильтром по patientId.
     * Результат отображается в {paymentsTable}.
     */
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

