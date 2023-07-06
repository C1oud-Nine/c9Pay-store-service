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

    /**
     * 사용자 토큰을 받아서 사용자 서비스를 통해 식별번호로 변경한 후,
     * 사용자가 보유한 모든 가게를 응답한다.
     *
     * @param userId 사용자 서비스를 통해 획득한 사용자 식별번호
     * @return 사용자의 모든 가게를 응답
     */
    @GetMapping
    public ResponseEntity<StoreDetailList> getStores(@RequestAttribute UUID userId) {
        List<StoreDetails> storeDetailsList = storeService.getAllStoreDetails(userId);
        return ResponseEntity.ok(new StoreDetailList(storeDetailsList));
    }

    /**
     * 사용자 토큰을 받아서 사용자 서비스를 통해 식별번호로 변경한 후,
     * 획득한 가게 정보를 토대로 가게를 만든다.
     * 그 후, 사용자가 보유한 모든 가게를 조회하여 응답한다.
     *
     * @param userId 사용자 서비스를 통해 획득한 사용자 식별번호
     * @param storeForm 가게를 생성하기 위한 가게 정보
     * @return 사용자의 모든 가게를 응답
     */
    @PostMapping
    public ResponseEntity<StoreDetailList> addStores(@RequestAttribute UUID userId, @RequestBody StoreForm storeForm) {
        // todo 가게 식별번호를 획득하는 로직 작성
        UUID storeId = UUID.randomUUID();
        Store store = storeService.createStore(storeId, userId, storeForm.getName());

        List<StoreDetails> storeDetailsList = storeService.getAllStoreDetails(userId);

        return ResponseEntity.ok(new StoreDetailList(storeDetailsList));
    }
}
