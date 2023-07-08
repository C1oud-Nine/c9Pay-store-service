package com.c9pay.storeservice.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Store {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private UUID storeId;
    private UUID userId;

    public Store(String name, UUID storeId, UUID userId) {
        this.name = name;
        this.storeId = storeId;
        this.userId = userId;
    }
}
