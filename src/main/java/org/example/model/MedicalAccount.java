package org.example.model;

import javafx.beans.property.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
public class MedicalAccount {
    private int id;
    private String number;
    private LocalDate dateCreated;
    private BigDecimal total;
    private BigDecimal rebate;
    private BigDecimal amountPaid;
    private String surname;
    private String firstname;
    private String middlename;
    private LocalDate birthDate;
    private String inn;
    private String category;

    // JavaFX Property-методы (нужны для TableView)
    public StringProperty numberProperty() { return new SimpleStringProperty(number); }
    public ObjectProperty<LocalDate> dateCreatedProperty() { return new SimpleObjectProperty<>(dateCreated); }
    public ObjectProperty<BigDecimal> totalProperty() { return new SimpleObjectProperty<>(total); }
    public StringProperty surnameProperty() { return new SimpleStringProperty(surname); }
    public StringProperty firstnameProperty() { return new SimpleStringProperty(firstname); }
    public StringProperty categoryProperty() { return new SimpleStringProperty(category); }

}
