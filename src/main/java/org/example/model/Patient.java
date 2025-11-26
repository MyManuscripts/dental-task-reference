package org.example.model;

import javafx.beans.property.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Patient {
    private int id;
    private String surname;
    private String firstname;
    private String middlename;
    private LocalDate birthDate;
    private String inn;
    private String cardNumber;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    // Для отображения в TableView: "Иванов И.И."
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(surname != null ? surname : "");
        if (firstname != null && !firstname.isEmpty()) {
            sb.append(" ").append(firstname.substring(0, 1)).append(".");
        }
        if (middlename != null && !middlename.isEmpty()) {
            sb.append(middlename.substring(0, 1)).append(".");
        }
        return sb.toString().trim();
    }

    // JavaFX Property-методы (для TableColumn)
    public StringProperty surnameProperty() { return new SimpleStringProperty(surname); }
    public StringProperty firstnameProperty() { return new SimpleStringProperty(firstname); }
    public StringProperty middlenameProperty() { return new SimpleStringProperty(middlename); }
    public ObjectProperty<LocalDate> birthDateProperty() { return new SimpleObjectProperty<>(birthDate); }
    public StringProperty innProperty() { return new SimpleStringProperty(inn); }
    public StringProperty cardNumberProperty() { return new SimpleStringProperty(cardNumber); }



    // Для совместимости с AccountDao (если будете связывать счета с пациентом)
    public String getPatientIdentifier() {
        return surname + " " + firstname + (middlename != null ? " " + middlename : "");
    }
}