package com.c9pay.storeservice.controller;

import com.c9pay.storeservice.dto.store.StoreDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/{store-id}")
public class LoginController {
    @PostMapping("/login")
    public ResponseEntity<StoreDetails> login(@PathVariable("store-id") int storeId, HttpServletResponse response) {
        response.addCookie(new Cookie("Authorization", "dummy-store-token"));

        // todo 사용자 식별번호 획득
        // todo store id 검증 로직

        return ResponseEntity.ok(new StoreDetails((long) storeId, "store1"));
    }
}
