package com.c9pay.storeservice.mvc.controller;

import com.c9pay.storeservice.data.dto.store.StoreDetails;
import com.c9pay.storeservice.jwt.TokenProvider;
import com.c9pay.storeservice.mvc.service.StoreService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<StoreDetails> login(@RequestAttribute UUID userId, @PathVariable("store-id") long storeId,
                                              HttpServletRequest request, HttpServletResponse response) {
        response.addCookie(new Cookie("Authorization", "dummy-store-token"));
        log.debug("userId: {}", userId);

        Optional<StoreDetails> storeDetailsOptional = storeService.getAllStoreDetails(userId).stream()
                .filter(storeDetails -> storeDetails.getId() == storeId)
                .findFirst();

        // 사용자의 가게가 맞는지 검증
        if (storeDetailsOptional.isPresent()) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(storeId, "");
            String token = tokenProvider.createToken(authentication, request.getRemoteAddr());
            response.addCookie(new Cookie(AUTHORIZATION_HEADER, "Bearer+"+token));

            return ResponseEntity.ok(storeDetailsOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

    }
}
