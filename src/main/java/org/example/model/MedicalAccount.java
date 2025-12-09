package org.example.model;

import javafx.beans.property.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import javafx.beans.property.*;

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
    private String doctorName = "";
    private String category;
    private boolean selected = true;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;

    // JavaFX Property-методы

    
    public StringProperty numberProperty() { return new SimpleStringProperty(number); }
    public ObjectProperty<LocalDate> dateCreatedProperty() { return new SimpleObjectProperty<>(dateCreated); }
    public ObjectProperty<BigDecimal> totalProperty() { return new SimpleObjectProperty<>(total); }
    public StringProperty surnameProperty() { return new SimpleStringProperty(surname); }
    public StringProperty firstnameProperty() { return new SimpleStringProperty(firstname); }
    public StringProperty categoryProperty() { return new SimpleStringProperty(category); }
    public BooleanProperty selectedProperty() { return new SimpleBooleanProperty(selected); }
    public void setSelected(boolean selected) { this.selected = selected; }
    public boolean isSelected() { return selected; }
    public ObjectProperty<BigDecimal> amountPaidProperty() {
        return new SimpleObjectProperty<>(amountPaid);
    }
    public void setCategory(String category) { this.category = category; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }


    public ObjectProperty<BigDecimal> paymentAmountProperty() {
        return new SimpleObjectProperty<>(paymentAmount);
    }

    public ObjectProperty<LocalDate> paymentDateProperty() {
        return new SimpleObjectProperty<>(paymentDate);
    }

    public StringProperty doctorNameProperty() {
        return new SimpleStringProperty(doctorName);
    }


    public ObjectProperty<BigDecimal> rebateProperty() {
        return new SimpleObjectProperty<>(rebate);
    }


}
