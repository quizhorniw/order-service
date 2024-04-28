package com.drevotyuk.model;

import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "CUSTOMERS")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Customer {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Exclude
    private int id;
    @NonNull
    private String surname;
    @NonNull
    private String firstname;
    @NonNull
    private String patronymic;
    @NonNull
    @EqualsAndHashCode.Exclude
    private LocalDate creationDate;
    @NonNull
    private String address;

    public Customer(@NonNull String surname, @NonNull String firstname, @NonNull String patronymic,
            @NonNull String address) {
        this.surname = surname;
        this.firstname = firstname;
        this.patronymic = patronymic;
        this.address = address;
    }
}
