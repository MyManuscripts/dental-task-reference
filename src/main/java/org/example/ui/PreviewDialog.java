
package org.example.ui;

import org.example.model.MedicalAccount;
import org.example.model.Patient;
import org.example.model.TaxReferenceSettings;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

/**
 * Диалог предварительного просмотра справки по форме КНД 1151156.
 * Отображает данные в точном соответствии с Приказом ФНС № ЕА-7-11/824 от 08.11.2023.
 * Использует HTML-шаблон + WebView для точного позиционирования.
 */
public class PreviewDialog {

    private final Stage dialog;
    private final TaxReferenceSettings settings;
    private final Patient patient;
    private final List<MedicalAccount> accounts;

    private static String formatWithSpaces(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        String s = String.format("%.2f", value).replace(',', '.'); // "2490.00"
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') {
                sb.append(" . ");
            } else {
                sb.append(c);
                if (i < s.length() - 3 && s.charAt(i + 1) != '.') {
                    sb.append(' ');
                }
            }
        }
        return sb.toString().trim();
    }

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
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // WebView для отображения HTML-формы
        WebView webView = new WebView();

        // WebView должен занимать всё доступное пространство
        webView.setPrefHeight(0); // позволяет растягиваться
        webView.setMinHeight(0);
        webView.setMaxHeight(Double.MAX_VALUE);

        VBox.setVgrow(webView, Priority.ALWAYS);

        // Внутри createScene(), после расчёта сумм:
        BigDecimal sumCode1 = accounts.stream()
                .map(MedicalAccount::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal sumCode2 = BigDecimal.ZERO;



        String sum1Formatted = formatWithSpaces(sumCode1);
        String sum2Formatted = formatWithSpaces(sumCode2);

        // Загружаем штрих-код как Base64
        String barcodeBase64 = null;
        URL barcodeUrl = getClass().getResource("/images/barcode.gif");
        if (barcodeUrl != null) {
            try (InputStream is = barcodeUrl.openStream()) {
                byte[] imageBytes = is.readAllBytes();
                barcodeBase64 = "data:image/gif;base64," + Base64.getEncoder().encodeToString(imageBytes);
            } catch (IOException e) {
                System.err.println("[Preview] Не удалось загрузить штрих-код: " + e.getMessage());
            }
        }
        String barcodePlaceholder = barcodeBase64 != null ? barcodeBase64 : "";

            // Загружаем шаблон БЕЗ toURI()
            URL resourceUrl = getClass().getResource("/templates/tax_form_1151156.html");
            if (resourceUrl == null) {
                throw new RuntimeException("Шаблон формы не найден: /templates/tax_form_1151156.html");
            }

            String template;
            try (InputStream is = resourceUrl.openStream()) {
                template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка чтения шаблона", e);
            }

            String clinicName = settings.getClinicName();
            if (clinicName == null || clinicName.trim().isEmpty()) {
                clinicName = "Наименование не указано";
            }

            // Заменяем плейсхолдеры на реальные данные
            String filled = template
                    .replace("[BARCODE_DATA_URI]", barcodePlaceholder)
                    .replace("[ИНН]", settings.getInn())
                    .replace("[КПП]", settings.getKpp())
                    .replace("[№ справки]", settings.getReferenceNumber())
                    .replace("[№ корректировки]", "0")
                    .replace("[Год]", String.valueOf(LocalDate.now().getYear()))
                    .replace("[НАИМЕНОВАНИЕ_ОРГАНИЗАЦИИ]", clinicName)
                    .replace("[ФАМИЛИЯ]", patient.getSurname())
                    .replace("[ИМЯ]", patient.getFirstname())
                    .replace("[ОТЧЕСТВО]", patient.getMiddlename())
                    .replace("[ИНН_НАЛОГОПЛАТЕЛЬЩИКА]", patient.getInn())
                    .replace("[ДАТА_РОЖДЕНИЯ]", patient.getBirthDate() != null ? patient.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "")
                    .replace("[КОД]", "12")
                    .replace("[СЕРИЯ_И_НОМЕР]", "АВ 123456")
                    .replace("[ДАТА_ВЫДАЧИ]", "01.01.2020")
                    .replace("[0 - нет]", "0")
                    .replace("[1 - да]", "1")
                    .replace("[СУММА_КОД_1]", sum1Formatted)
                    .replace("[СУММА_КОД_2]", sum2Formatted)  // ← замените на сумму по коду 2
                    .replace("[Подпись]", settings.getEcpSignerName())
                    .replace("[Дата подписи]", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    .replace("[Кол-во страниц]", "2")
                    .replace("[ФАМИЛИЯ]", patient.getSurname())
                    .replace("[ИМЯ]", patient.getFirstname())
                    .replace("[ОТЧЕСТВО]", patient.getMiddlename());

            webView.getEngine().loadContent(filled);

            // Кнопки
        Button printButton = new Button("Печать");
        Button closeButton = new Button("Закрыть");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(printButton, closeButton);

        // Сборка
        root.getChildren().add(webView);
        root.getChildren().add(buttonBox);

        // Обработчики
        printButton.setOnAction(e -> {
            status("Печать — в разработке");
        });
        closeButton.setOnAction(e -> dialog.close());

        Scene scene = new Scene(root, 800, 900); // начальная ширина/высота
        scene.setRoot(root); // ← важно!
        return scene;
    }

    private void status(String msg) {
        System.out.println("[Preview] " + msg);
    }

    public void show() {
        dialog.showAndWait();
    }


}




