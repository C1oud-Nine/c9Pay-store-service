package com.c9pay.storeservice.proxy;

import com.c9pay.storeservice.data.dto.certificate.PublicKeyResponse;
import com.c9pay.storeservice.data.dto.proxy.SerialNumberResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name="auth-service", url="${AUTH_SERVICE_URI:http://localhost}:8081")
public interface AuthServiceProxy {
    @PostMapping("/auth-service/serial-number")
    public ResponseEntity<SerialNumberResponse> createSerialNumber();

    @GetMapping("/auth-service/serial-number")
    public ResponseEntity<?> verifySerialNumber(@RequestParam UUID serialNumber);

    @GetMapping("/auth-service/public-key")
    public ResponseEntity<PublicKeyResponse> getPublicKey();
}
