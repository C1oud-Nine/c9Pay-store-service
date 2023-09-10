package com.c9pay.storeservice.mvc.controller;

import com.c9pay.storeservice.config.Resilience4JConfig;
import com.c9pay.storeservice.constant.GatewayConstant;
import com.c9pay.storeservice.data.dto.proxy.SerialNumberResponse;
import com.c9pay.storeservice.data.dto.store.StoreDetailList;
import com.c9pay.storeservice.data.dto.store.StoreDetails;
import com.c9pay.storeservice.data.dto.store.StoreForm;
import com.c9pay.storeservice.interceptor.GatewayValidation;
import com.c9pay.storeservice.mvc.service.StoreService;
import com.c9pay.storeservice.proxy.AuthServiceProxy;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.c9pay.storeservice.config.Resilience4JConfig.circuitBreakerThrowable;
import static com.c9pay.storeservice.constant.GatewayConstant.API;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/store")
public class StoreController {
    private final StoreService storeService;

    private final CircuitBreakerFactory circuitBreakerFactory;
    private final AuthServiceProxy authServiceProxy;

    /**
     * 사용자 토큰을 받아서 사용자 서비스를 통해 식별번호로 변경한 후,
     * 사용자가 보유한 모든 가게를 응답한다.
     *
     * @param userId 사용자 서비스를 통해 획득한 사용자 식별번호
     * @return 사용자의 모든 가게를 응답
     */
    @GetMapping
    @GatewayValidation(API)
    @RateLimiter(name = "Rate_limiter")
    public ResponseEntity<StoreDetailList> getStores(Principal principal) {
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
        UUID userId = UUID.fromString(principal.getName());
        List<StoreDetails> storeDetailsList = circuitbreaker.run(() -> storeService.getAllStoreDetails(userId),
                throwable -> circuitBreakerThrowable());
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
    @GatewayValidation(API)
    @RateLimiter(name = "Rate_limiter")
    public ResponseEntity<StoreDetailList> addStores(Principal principal, @RequestBody StoreForm storeForm) {
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");

        ResponseEntity<SerialNumberResponse> serialNumberResponse = circuitbreaker.run(authServiceProxy::createSerialNumber,
                throwable -> circuitBreakerThrowable());
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
