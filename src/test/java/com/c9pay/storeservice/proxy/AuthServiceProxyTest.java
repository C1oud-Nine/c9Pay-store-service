package com.c9pay.storeservice.proxy;

import com.c9pay.storeservice.data.dto.proxy.SerialNumberResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceProxyTest {
    @Autowired AuthServiceProxy authServiceProxy;

    @Test
    public void proxyTest() {
        ResponseEntity<SerialNumberResponse> serialNumber = authServiceProxy.createSerialNumber();
        assertTrue(authServiceProxy.verifySerialNumber(serialNumber.getBody().getSerialNumber()).getStatusCode() == HttpStatusCode.valueOf(200));
    }
}