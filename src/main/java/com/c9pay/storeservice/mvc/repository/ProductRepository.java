package com.c9pay.storeservice.mvc.repository;

import com.c9pay.storeservice.data.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByStoreId(Long storeId);
}
