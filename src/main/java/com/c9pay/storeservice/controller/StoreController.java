package com.c9pay.storeservice.controller;

import com.c9pay.storeservice.dto.store.StoreDetailList;
import com.c9pay.storeservice.dto.store.StoreDetails;
import com.c9pay.storeservice.dto.store.StoreForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/store")
public class StoreController {
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
        // todo 가게 추가 로직 작성
        log.info("storeForm: {}", storeForm);
        List<StoreDetails> storeDetailsList = List.of(
                new StoreDetails(1L, "store1"),
                new StoreDetails(2L, "store2"),
                new StoreDetails(3L, "store3"),
                new StoreDetails(4L, storeForm.getName())
        );

        return ResponseEntity.ok(new StoreDetailList(storeDetailsList));
    }
}
