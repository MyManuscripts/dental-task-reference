package org.example.ui;

import javafx.beans.property.SimpleStringProperty;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private Map<String, Integer> practiceMap = new HashMap<>();

    public Scene getScene() {
        initUI();
        VBox root = buildLayout();
        Scene scene = new Scene(root, 1000, 700);
        return scene;
    }
    private void showPatientSelectionDialog(List<Patient> patients) {
        Stage stage = new Stage();
        stage.setTitle("Выберите пациента");

        TableView<Patient> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(patients));

        TableColumn<Patient, String> nameCol = new TableColumn<>("Пациент");
        nameCol.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getFullName()) // surname + " " + firstname
        );

        TableColumn<Patient, String> cardCol = new TableColumn<>("№ карты");
        cardCol.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getCardNumber())
        );

        table.getColumns().add(cardCol);

        TableColumn<Patient, String> dobCol = new TableColumn<>("Дата рождения");
        dobCol.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getBirthDate() != null
                        ? p.getValue().getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                        : "")
        );

        table.getColumns().addAll(nameCol, dobCol);

        Button selectBtn = new Button("Выбрать");
        selectBtn.setOnAction(e -> {
            Patient selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Сохраняем выбранного пациента в настройках
                settings.setSelectedPatient(selected);
                statusLabel.setText("✅ Выбран пациент: " + selected.getFullName() + " (№ карты: " + selected.getCardNumber() + ")");
                stage.close();
            } else {
                statusLabel.setText("⚠️ Выберите пациента из списка");
            }
        });

        VBox root = new VBox(10, table, selectBtn);
        root.setPadding(new Insets(10));
        stage.setScene(new Scene(root, 500, 400));
        stage.show();
    }

    private void initUI() {


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
                statusLabel.setText("⚠️ Введите ФИО или номер карты");
                return;
            }

            String selectedPractice = practiceComboBox.getValue();
            int practiceId = 0; // Все филиалы

            if (selectedPractice != null && !"Все филиалы".equals(selectedPractice)) {
                practiceId = practiceMap.get(selectedPractice); // ← берём ID из map
            }

            final int finalPracticeId = practiceId;
            final String finalQuery = query;

            new Thread(() -> {
                try {
                    List<Patient> patients = accountDao.findPatientsByQuery(finalPracticeId, finalQuery);
                    final List<Patient> finalPatients = new ArrayList<>(patients);
                    javafx.application.Platform.runLater(() -> {
                        // Открываем окно выбора пациента
                        showPatientSelectionDialog(patients);
                    });
                } catch (SQLException ex) {
                    javafx.application.Platform.runLater(() ->
                            statusLabel.setText("⚠️ Ошибка поиска: " + ex.getMessage())
                    );
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
                    practiceComboBox.setValue("Все филиалы");
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() ->
                        statusLabel.setText("⚠️ Ошибка загрузки филиалов: " + e.getMessage())
                );
            }
        }).start();
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
        Label title = new Label("Сведения о налогоплательщике");
        title.setStyle("-fx-font-weight: bold;");
        box.getChildren().addAll(title, patientIsPayerCheckBox);
        return box;
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

        paymentsTable.getColumns().addAll(selectCol, numberCol, patientCol, dateCol, totalCol, paidCol, categoryCol);
        paymentsTable.setItems(paymentsData);
        paymentsTable.setPlaceholder(new Label("Нажмите «Показать платежи пациента»"));
    }

    private void loadPayments() {
        String selectedPractice = practiceComboBox.getValue();
        int practiceId = 0;



        if (selectedPractice != null && !selectedPractice.startsWith("Все")) {

            practiceId = Integer.parseInt(selectedPractice.replace("Филиал ", ""));
        }
        final int finalPracticeId = practiceId;
        int year = yearComboBox.getValue();

        new Thread(() -> {
            try {
                List<String> categories = new ArrayList<>(settings.getSelectedCategories());
                if (categories.isEmpty()) {
                    categories.add("Терапия");
                }

                List<MedicalAccount> accounts = accountDao.findAccountsForTaxReport(

                        finalPracticeId,
                        LocalDate.of(year, 1, 1),
                        LocalDate.of(year, 12, 31),
                        categories
                );

                javafx.application.Platform.runLater(() -> {
                    paymentsData.setAll(accounts);
                    statusLabel.setText("✅ Найдено " + accounts.size() + " платежей за " + year + " г.");
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() ->
                        statusLabel.setText("⚠️ Ошибка: " + e.getMessage())
                );
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

