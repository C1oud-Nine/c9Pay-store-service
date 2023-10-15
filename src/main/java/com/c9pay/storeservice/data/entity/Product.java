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
    private String imageUrl;

    public Product(String name, int price, Store store) {
        this.name = name;
        this.price = price;
        this.store = store;
    }

    public Product(String name, int price, Store store, String imageUrl) {
        this.name = name;
        this.price = price;
        this.store = store;
        this.imageUrl = imageUrl;
    }

    public void updateProduct(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public void updateProduct(String name, int price, String imageUrl) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}
