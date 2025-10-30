package org.example.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MedicalAccount {
    private int id;
    private String number;
    private Date dateCreated;
    private BigDecimal total;
    private BigDecimal rebate;
    private BigDecimal amountPaid;
    private String surname;
    private String firstname;
    private String middlename;
    private Date birthDate;
    private String inn;



}
