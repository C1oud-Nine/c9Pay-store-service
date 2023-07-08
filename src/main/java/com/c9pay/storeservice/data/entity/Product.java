package com.c9pay.storeservice.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Product {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private int price;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    public Product(String name, int price, Store store) {
        this.name = name;
        this.price = price;
        this.store = store;
    }

    public void updateProduct(String name, int price) {
        this.name = name;
        this.price = price;
    }
}
