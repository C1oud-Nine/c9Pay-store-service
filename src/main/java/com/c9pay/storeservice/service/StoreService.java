package com.c9pay.storeservice.service;

import com.c9pay.storeservice.dto.store.StoreDetails;
import com.c9pay.storeservice.entity.Store;
import com.c9pay.storeservice.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    public Store createStore(UUID storeId, UUID userId, String name) {
        return storeRepository.save(new Store(name, storeId, userId));
    }

    public List<StoreDetails> getAllStoreDetails(UUID userId) {
        return storeRepository.findAllByUserId(userId).stream()
                .map((s)->new StoreDetails(s.getId(), s.getName()))
                .toList();
    }
}
