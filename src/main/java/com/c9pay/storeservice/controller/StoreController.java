package com.c9pay.storeservice.controller;

import com.c9pay.storeservice.dto.store.StoreDetailList;
import com.c9pay.storeservice.dto.store.StoreDetails;
import com.c9pay.storeservice.dto.store.StoreForm;
import com.c9pay.storeservice.entity.Store;
import com.c9pay.storeservice.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/store")
public class StoreController {
    private final StoreService storeService;
    @GetMapping
    public ResponseEntity<StoreDetailList> getStores() {
        // todo Store 목록 조회 작성
        List<StoreDetails> storeDetailsList = List.of(
                new StoreDetails(1L, "store1"),
                new StoreDetails(2L, "store2"),
                new StoreDetails(3L, "store3")
        );

        return ResponseEntity.ok(new StoreDetailList(storeDetailsList));
    }

    @PostMapping
    public ResponseEntity<StoreDetailList> addStores(@RequestBody StoreForm storeForm) {
        // todo filter단에서 사용자 토큰을 식별번호로 변경
        UUID userId = storeForm.getUserId();

        // todo 가게 식별번호를 획득하는 로직 작성
        UUID storeId = UUID.randomUUID();
        Store store = storeService.createStore(storeId, userId, storeForm.getName());

        List<StoreDetails> storeDetailsList = storeService.getAllStoreDetails(userId);

        return ResponseEntity.ok(new StoreDetailList(storeDetailsList));
    }
}
