
package org.example.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.MedicalAccount;
import org.example.model.Patient;
import org.example.model.TaxReferenceSettings;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.scene.control.*;
import javafx.scene.layout.*;


public class PreviewDialog {

    private final Stage dialog;
    private final TaxReferenceSettings settings;
    private final Patient patient;
    private final List<MedicalAccount> accounts;

    public PreviewDialog(TaxReferenceSettings settings, Patient patient, List<MedicalAccount> accounts, Stage owner) {
        this.settings = settings;
        this.patient = patient;
        this.accounts = accounts;
        this.dialog = new Stage();
        this.dialog.initOwner(owner);
        this.dialog.initModality(Modality.APPLICATION_MODAL);
        this.dialog.setTitle("Предварительный просмотр справки");
        this.dialog.setScene(createScene());
    }

    private Scene createScene() {
        // Основная панель — имитация печатного листа А4
        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-border-color: #333; -fx-border-width: 1px; -fx-background-color: white;");

        // === 1. Шапка справки ===
        Label header = new Label("СПРАВКА\nо доходах, расходах и суммах налоговых вычетов");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-alignment: center;");
        header.setAlignment(Pos.CENTER);
        header.setWrapText(true);

        // === 2. Данные налогоплательщика (из настроек) ===
        HBox payerBox = new HBox(10);
        payerBox.setAlignment(Pos.CENTER_LEFT);
        payerBox.getChildren().addAll(
                new Label("ИНН налогоплательщика: "),
                new TextField(settings.getInn())
        );

        // === 3. Данные пациента ===
        GridPane patientGrid = new GridPane();
        patientGrid.setHgap(10);
        patientGrid.setVgap(5);
        patientGrid.add(new Label("ФИО пациента:"), 0, 0);
        patientGrid.add(new TextField(patient.getFullName()), 1, 0);
        patientGrid.add(new Label("Дата рождения:"), 0, 1);
        patientGrid.add(new TextField(
                patient.getBirthDate() != null
                        ? patient.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                        : ""
        ), 1, 1);
        patientGrid.add(new Label("ИНН пациента:"), 0, 2);
        patientGrid.add(new TextField(patient.getInn()), 1, 2);

        // === 4. Платежи (таблица) ===
        TableView<MedicalAccount> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(accounts));
        TableColumn<MedicalAccount, String> numCol = new TableColumn<>("№ счёта");
        numCol.setCellValueFactory(c -> c.getValue().numberProperty());
        TableColumn<MedicalAccount, LocalDate> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(c -> c.getValue().dateCreatedProperty());
        TableColumn<MedicalAccount, BigDecimal> sumCol = new TableColumn<>("Сумма");
        sumCol.setCellValueFactory(c -> c.getValue().totalProperty());

        // Формат суммы
        sumCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("%.2f", item));
            }
        });

        table.getColumns().addAll(numCol, dateCol, sumCol);
        table.setPrefHeight(200);

        // === 5. Итоги ===
        BigDecimal total = accounts.stream()
                .map(MedicalAccount::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_LEFT);
        totalBox.getChildren().addAll(
                new Label("Общая сумма расходов: "),
                new TextField(String.format("%.2f", total))
        );

        // === 6. Подпись (из настроек) ===
        HBox signerBox = new HBox(10);
        signerBox.setAlignment(Pos.CENTER_LEFT);
        signerBox.getChildren().addAll(
                new Label("Подпись: "),
                new TextField(settings.getEcpSignerName())
        );

        // === Сборка ===
        content.getChildren().addAll(header, payerBox, patientGrid, new Label("Платежи:"), table, totalBox, signerBox);

        // === Кнопки внизу ===
        Button printBtn = new Button("Печать");
        Button closeBtn = new Button("Закрыть");
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(printBtn, closeBtn);

        VBox root = new VBox(20, content, buttonBox);
        root.setPadding(new Insets(20));

        // Обработчики
        printBtn.setOnAction(e -> {
            // TODO: реализовать печать (PrinterJob или PDF-экспорт)
            status("Печать — в разработке");
        });
        closeBtn.setOnAction(e -> dialog.close());

        return new Scene(root, 800, 700);
    }

    private void status(String msg) {
        // В будущем — можно добавить status bar в PreviewDialog
        System.out.println("[Preview] " + msg);
    }

    public void show() {
        dialog.showAndWait();
    }
}




