package org.example.ui;

import org.example.dao.PracticeDao;
import org.example.model.TaxReferenceSettings;
import org.example.dao.PracticeDao.PracticeInfo;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsDialog {
    private final ComboBox<String> practiceCombo = new ComboBox<>();
    private final PracticeDao practiceDao = new PracticeDao();
    private final TaxReferenceSettings settings;
    private final Stage stage;
    private final TextField innField = new TextField();
    private final TextField kppField = new TextField();
    private final TextField creatorField = new TextField();
    private final TextField copiesField = new TextField();
    private final TextField taxOrgCodeField = new TextField();
    private final ToggleGroup orgTypeGroup = new ToggleGroup();
    private final RadioButton orgRadio = new RadioButton("Организация");
    private final RadioButton ipRadio = new RadioButton("ИП");
    private final ToggleGroup signerTypeGroup = new ToggleGroup();
    private final RadioButton headRadio = new RadioButton("Руководитель");
    private final RadioButton repRadio = new RadioButton("Представитель");
    private final TextField documentField = new TextField();
    private final TextField ecpField = new TextField();
    private final TextField exportPathField = new TextField();
    private final TextField procedureTypeField = new TextField();
    private Map<String, PracticeDao.PracticeInfo> practiceInfoMap = new HashMap<>();

    private void updateInnKpp(String practiceName) {
        PracticeDao.PracticeInfo info = practiceInfoMap.get(practiceName);
        if (info != null) {
            innField.setText(info.inn);
            kppField.setText(info.kpp);
        } else {
            innField.clear();
            kppField.clear();
        }
    }

    public SettingsDialog(TaxReferenceSettings settings, Stage owner) {
        this.settings = settings;
        this.stage = new Stage();
        this.stage.setTitle("Настройки справки");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // === 1. Выбор филиала — ПЕРВЫЙ элемент ===
        Label practiceLabel = new Label("Филиал:");
        practiceCombo.setPrefWidth(300);


        // Загружаем филиалы в фоновом потоке — с ИНН и КПП
        new Thread(() -> {
            try {
                List<PracticeInfo> practices = practiceDao.loadPracticeInfoList(); // ← изменено!
                Platform.runLater(() -> {
                    practiceInfoMap.clear();
                    practiceCombo.getItems().clear();


                    // Добавляем "Все филиалы"
                    PracticeInfo all = new PracticeInfo(0, "Все филиалы", "", "");
                    practiceInfoMap.put(all.name, all);
                    practiceCombo.getItems().add(all.name);

                    // Добавляем реальные филиалы
                    for (PracticeInfo p : practices) {
                        practiceCombo.getItems().add(p.name);
                        practiceInfoMap.put(p.name, p);
                    }

                    // Восстанавливаем сохранённый выбор
                    String saved = settings.getSelectedPractice();
                    if (saved != null && practiceInfoMap.containsKey(saved)) {
                        practiceCombo.setValue(saved);
                        updateInnKpp(saved); // ← сразу подставляем ИНН/КПП!
                    } else {
                        practiceCombo.setValue("Все филиалы");
                        innField.clear();
                        kppField.clear();
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() ->
                        new Alert(Alert.AlertType.ERROR, "Ошибка загрузки филиалов: " + e.getMessage()).show());
            }
        }).start();

        // Подписка на изменение выбора
        practiceCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateInnKpp(newVal);
            }
        });


        // === 2. Остальные поля ===
        innField.setText(settings.getInn());
        kppField.setText(settings.getKpp());
        creatorField.setText(settings.getCreatorFullName());
        copiesField.setText(String.valueOf(settings.getCopiesCount()));
        taxOrgCodeField.setText(settings.getTaxOrgCode());
        orgRadio.setSelected(settings.getOrgType() == 1);
        ipRadio.setSelected(settings.getOrgType() == 2);
        headRadio.setSelected(settings.getSignerType() == 1);
        repRadio.setSelected(settings.getSignerType() == 2);
        documentField.setText(settings.getDocumentName());
        ecpField.setText(settings.getEcpSignerName());
        exportPathField.setText(settings.getExportPath());
        procedureTypeField.setText(String.valueOf(settings.getProcedureType()));

        orgTypeGroup.getToggles().addAll(orgRadio, ipRadio);
        signerTypeGroup.getToggles().addAll(headRadio, repRadio);

        // === 3. Собираем интерфейс ===
        root.getChildren().addAll(
                practiceLabel, practiceCombo,
                new Text("ИНН:"), innField,
                new Text("КПП:"), kppField,
                new Text("Оформитель справки (ФИО):"), creatorField,
                new Text("Количество экземпляров справки:"), copiesField,
                new Text("4-значный код налогового органа:"), taxOrgCodeField,
                new Text("Тип организации:"), orgRadio, ipRadio,
                new Text("Тип лица, подписавшего документ:"), headRadio, repRadio,
                new Text("Наименование и номер документа представителя:"), documentField,
                new Text("ФИО лица, который поставит ЭЦП:"), ecpField,
                new Text("Путь к папке для выгрузки файлов (например: D:\\Файлы\\Файлы пациентов):"), exportPathField,
                new Text("Тип процедур (1 или 2):"), procedureTypeField,

                new Button("Ок") {{
                    setOnAction(e -> saveAndClose());
                }},
                new Button("Отмена") {{
                    setOnAction(e -> stage.close());
                }}
        );

        stage.setScene(new Scene(root, 500, 750)); // чуть выше — поместится филиал
        stage.initOwner(owner);
        stage.showAndWait();
    }

    private void saveAndClose() {
        settings.setInn(innField.getText());
        settings.setKpp(kppField.getText());
        settings.setCreatorFullName(creatorField.getText());
        settings.setCopiesCount(Integer.parseInt(copiesField.getText()));
        settings.setTaxOrgCode(taxOrgCodeField.getText());
        settings.setOrgType(orgRadio.isSelected() ? 1 : 2);
        settings.setSignerType(headRadio.isSelected() ? 1 : 2);
        settings.setDocumentName(documentField.getText());
        settings.setEcpSignerName(ecpField.getText());
        settings.setExportPath(exportPathField.getText());
        settings.setProcedureType(Integer.parseInt(procedureTypeField.getText()));
        settings.setSelectedPractice(practiceCombo.getValue());
        stage.close();
    }
}