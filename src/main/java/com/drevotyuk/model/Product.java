package com.drevotyuk.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "PRODUCTS")
@NoArgsConstructor
@Data
public class Product {
    @Id
    @NonNull
    private String name;
    @NonNull
    private String description;
    @NonNull
    private double price;
    @NonNull
    private int quantity;

    public Product(@NonNull String name, @NonNull String description, @NonNull double price,
            @NonNull int quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }
}
