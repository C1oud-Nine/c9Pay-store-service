package com.c9pay.storeservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "user-service", url = "${USER_SERVICE_URI:http://localhost}:8083")
public interface UserServiceProxy {
    @GetMapping("/user-service/api/user/serial-number")
    public ResponseEntity<?> getSerialNumber(String token);
}
