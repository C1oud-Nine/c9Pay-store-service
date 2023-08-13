package com.c9pay.storeservice.mvc.controller;

import com.c9pay.storeservice.data.dto.proxy.SerialNumberResponse;
import com.c9pay.storeservice.data.dto.store.StoreDetailList;
import com.c9pay.storeservice.data.dto.store.StoreDetails;
import com.c9pay.storeservice.data.dto.store.StoreForm;
import com.c9pay.storeservice.mvc.service.StoreService;
import com.c9pay.storeservice.proxy.AuthServiceProxy;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/store")
public class StoreController {
    private final StoreService storeService;
    private final AuthServiceProxy authServiceProxy;

    /**
     * 사용자 토큰을 받아서 사용자 서비스를 통해 식별번호로 변경한 후,
     * 사용자가 보유한 모든 가게를 응답한다.
     *
     * @param userId 사용자 서비스를 통해 획득한 사용자 식별번호
     * @return 사용자의 모든 가게를 응답
     */
    @GetMapping
    public ResponseEntity<StoreDetailList> getStores(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
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
    public ResponseEntity<StoreDetailList> addStores(Principal principal, @RequestBody StoreForm storeForm) {
        ResponseEntity<SerialNumberResponse> serialNumberResponse = authServiceProxy.createSerialNumber();
        UUID userId = UUID.fromString(principal.getName());
        log.info("UUID: {}", userId);
        Optional<SerialNumberResponse> responseOptional = Optional.ofNullable(serialNumberResponse.getBody());

        return responseOptional
                .map(SerialNumberResponse::getSerialNumber)
                .map((id)->storeService.createStore(id, userId, storeForm.getName()))
                .map((store)->storeService.getAllStoreDetails(userId))
                .map((details)->ResponseEntity.ok(new StoreDetailList(details)))
                .orElse(ResponseEntity.badRequest().build());
    }
}
