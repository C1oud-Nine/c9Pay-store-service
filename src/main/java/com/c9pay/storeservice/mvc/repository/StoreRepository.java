package com.c9pay.storeservice.mvc.repository;

import com.c9pay.storeservice.data.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, Long> {
    public List<Store> findAllByUserId(UUID userId);
}
