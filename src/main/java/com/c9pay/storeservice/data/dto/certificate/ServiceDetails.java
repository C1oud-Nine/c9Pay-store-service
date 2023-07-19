package com.c9pay.storeservice.data.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDetails {
    private String publicKey;
    private ServiceInfo serviceInfo;
}
