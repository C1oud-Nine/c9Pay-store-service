package com.c9pay.storeservice.mvc.controller;

import com.c9pay.storeservice.data.dto.auth.StoreToken;
import com.c9pay.storeservice.data.dto.store.StoreDetails;
import com.c9pay.storeservice.jwt.TokenProvider;
import com.c9pay.storeservice.mvc.service.StoreService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import static com.c9pay.storeservice.constant.CookieConstant.AUTHORIZATION_HEADER;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/{store-id}")
public class LoginController {
    private final StoreService storeService;
    private final TokenProvider tokenProvider;
    @PostMapping("/login")
    public ResponseEntity<StoreToken> login(Principal principal, @PathVariable("store-id") long storeId,
                                              HttpServletRequest request, HttpServletResponse response) {
        UUID userId = UUID.fromString(principal.getName());
        log.debug("userId: {}", userId);

        Optional<StoreDetails> storeDetailsOptional = storeService.getAllStoreDetails(userId).stream()
                .filter(storeDetails -> storeDetails.getId() == storeId)
                .findFirst();

        // 사용자의 가게가 맞는지 검증
        if (storeDetailsOptional.isPresent()) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(storeId, "");
            String token = "Bearer+" + tokenProvider.createToken(authentication, request.getRemoteAddr());

            return ResponseEntity.ok(new StoreToken(token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

    }
}
