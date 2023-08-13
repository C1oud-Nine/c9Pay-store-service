package com.c9pay.storeservice.mvc.service;

import com.c9pay.storeservice.data.dto.store.StoreDetails;
import com.c9pay.storeservice.data.entity.Store;
import com.c9pay.storeservice.mvc.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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

    public Optional<UUID> getOwnerId(Long storeId) {
        Optional<Store> storeOptional = storeRepository.findById(storeId);
        return storeOptional.map(Store::getUserId);
    }

    public Optional<Store> findStore(Long storeId) {
        return storeRepository.findById(storeId);
    }
}
