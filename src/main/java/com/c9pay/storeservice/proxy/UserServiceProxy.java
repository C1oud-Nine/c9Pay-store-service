package com.c9pay.storeservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.c9pay.storeservice.constant.CookieConstant.AUTHORIZATION_HEADER;

@FeignClient(name = "user-service")
public interface UserServiceProxy {
    @GetMapping("/user-service/api/user/serial-number")
    public ResponseEntity<String> getSerialNumber(@CookieValue(AUTHORIZATION_HEADER) String token);
}
