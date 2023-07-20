package com.c9pay.storeservice.certificate;

import com.c9pay.storeservice.proxy.AuthServiceProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PublicKeyProviderFactory {
    private final KeyAlgorithm rsa;
    private final AuthServiceProxy authServiceProxy;

    @Bean
    public PublicKeyProvider authServicePublicKeyProvider() {
        return new AuthServicePublicKeyProvider(rsa, authServiceProxy);
    }

    public PublicKeyProvider generalPublicKeyProvider(String keyString) {
        return new GeneralPublicKeyProvider(rsa, keyString);
    }
}
