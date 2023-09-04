package com.c9pay.storeservice.proxy;

import com.c9pay.storeservice.data.dto.charge.ChargeAmount;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="credit-service")
public interface CreditServiceProxy {
    @PostMapping("/credit-service/account/{to}/transfer/{from}")
    public ResponseEntity transfer(@PathVariable(name= "to") String to,
                                      @PathVariable(name="from") String from,
                                      @RequestBody ChargeAmount chargeAmount);
}
