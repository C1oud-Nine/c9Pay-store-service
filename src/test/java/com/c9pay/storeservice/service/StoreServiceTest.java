package com.c9pay.storeservice.service;

import com.c9pay.storeservice.dto.store.StoreDetails;
import com.c9pay.storeservice.entity.Store;
import com.c9pay.storeservice.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {
    @Mock
    StoreRepository storeRepository;
    @InjectMocks
    StoreService storeService;

    UUID userId, storeId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        storeId = UUID.randomUUID();
    }

    @Test
    void 가게_생성() {
        // given
        long expectedIndex = 1L;
        given(storeRepository.save(any())).willAnswer((a)->{
            Store store = (Store) a.getArgument(0);
            return new Store(expectedIndex, store.getName(), store.getStoreId(), store.getUserId());
        });

        Store expected = new Store("store", storeId, userId);

        // when
        Store store = storeService.createStore(expected.getStoreId(), expected.getUserId(), expected.getName());

        // then
        assertEquals(expectedIndex, store.getId());
        assertEquals(expected.getName(), store.getName());
        assertEquals(expected.getUserId(), store.getUserId());
        assertEquals(expected.getStoreId(), store.getStoreId());
    }

    @Test
    void 가게조회() {
        // given
        long index = 1L;
        List<Store> stores = List.of(
                new Store(index, "store" + index++, UUID.randomUUID(), userId),
                new Store(index, "store" + index++, UUID.randomUUID(), userId),
                new Store(index, "store" + index++, UUID.randomUUID(), userId),
                new Store(index, "store" + index++, UUID.randomUUID(), UUID.randomUUID()),
                new Store(index, "store" + index++, UUID.randomUUID(), UUID.randomUUID()),
                new Store(index, "store" + index++, UUID.randomUUID(), UUID.randomUUID())
        );
        given(storeRepository.findAllByUserId(any())).willAnswer((a)->{
            UUID uuid = (UUID) a.getArgument(0);
            return stores.stream().filter((s)->uuid.equals(s.getUserId())).toList();
        });
        List<StoreDetails> expected = stores.stream().filter((s)->s.getUserId().equals(userId))
                .map((s)->new StoreDetails(s.getId(), s.getName())).toList();

        // when
        List<StoreDetails> storeDetails = storeService.getAllStoreDetails(userId);

        // then
        assertEquals(expected.size(), storeDetails.size());
        assertIterableEquals(expected, storeDetails);
    }
}