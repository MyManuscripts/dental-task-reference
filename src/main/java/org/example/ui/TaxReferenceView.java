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
import org.example.model.TaxReferenceSettings;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


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

    public Scene getScene() {
        initUI();
        VBox root = buildLayout();
        Scene scene = new Scene(root, 1000, 700);
        return scene;
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
            statusLabel.setText("ℹ️ Поиск пациента — в разработке");
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

        HBox.setMargin(settingsButton, new Insets(0, 0, 0, 360));

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

        int year = yearComboBox.getValue();

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