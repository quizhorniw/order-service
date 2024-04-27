package com.drevotyuk.model;

import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Customer {
    @Id
    @GeneratedValue
    private int id;
    @NonNull
    private String firstname;
    @NonNull
    private String surname;
    @NonNull
    private String lastname;
    @NonNull
    private LocalDate creationDate;
    @NonNull
    private String address;

    public Customer(@NonNull String firstname, @NonNull String surname, @NonNull String lastname,
            @NonNull LocalDate creationDate, @NonNull String address) {
        this.firstname = firstname;
        this.surname = surname;
        this.lastname = lastname;
        this.creationDate = creationDate;
        this.address = address;
    }
}
