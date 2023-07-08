package com.c9pay.storeservice.repository;

import com.c9pay.storeservice.data.entity.Product;
import com.c9pay.storeservice.data.entity.Store;
import com.c9pay.storeservice.mvc.repository.ProductRepository;
import com.c9pay.storeservice.mvc.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class StoreRepositoryTest {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    StoreRepository storeRepository;

    @Test
    void store_id로_product_조회() {
        // given
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        List<Store> stores = List.of(
                new Store("store1", UUID.randomUUID(), user1),
                new Store("store2", UUID.randomUUID(), user1),
                new Store("store3", UUID.randomUUID(), user2)
        );
        storeRepository.saveAll(stores);

        List<Product> products = List.of(
                new Product("p1", 1000, stores.get(0)),
                new Product("p2", 1000, stores.get(0)),
                new Product("p3", 1000, stores.get(0)),
                new Product("p4", 10000, stores.get(1)),
                new Product("p5", 10000, stores.get(1)),
                new Product("p6", 1000, stores.get(2))
        );
        productRepository.saveAll(products);

        List<Product> expected = products.stream()
                .filter((s)->s.getStore().equals(stores.get(0)))
                .toList();

        // when
        List<Product> found = productRepository.findAllByStoreId(stores.get(0).getId());

        // then
        assertIterableEquals(expected, found);
    }
}