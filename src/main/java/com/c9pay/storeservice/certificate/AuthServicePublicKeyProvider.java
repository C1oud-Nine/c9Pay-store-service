package com.c9pay.storeservice.certificate;

import com.c9pay.storeservice.data.dto.certificate.PublicKeyResponse;
import com.c9pay.storeservice.proxy.AuthServiceProxy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

@Slf4j
public class AuthServicePublicKeyProvider implements PublicKeyProvider {
    private final KeyAlgorithm keyAlgorithm;
    private final AuthServiceProxy authServiceProxy;
    private Optional<PublicKey> publicKeyOptional;

    public AuthServicePublicKeyProvider(KeyAlgorithm keyAlgorithm, AuthServiceProxy authServiceProxy) {
        this.keyAlgorithm = keyAlgorithm;
        this.authServiceProxy = authServiceProxy;
        publicKeyOptional = requestPublicKey();
    }

    @Override
    public PublicKey getPublicKey() throws KeyAcquisitionFailureException {
        return publicKeyOptional
                .or(this::requestPublicKey)
                .orElseThrow(()->new KeyAcquisitionFailureException("인증 서비스로부터 키를 획득하지 못했습니다."));
    }

    private Optional<PublicKey> requestPublicKey() {
        Optional<PublicKeyResponse> responseOptional = Optional.ofNullable(authServiceProxy.getPublicKey().getBody());
        return responseOptional.map(PublicKeyResponse::getPublicKey)
                .map(this::getPublicKeyFromBase64String);
    }

    @Override
    public Signature getSignature() {
        return keyAlgorithm.getSignature();
    }

    @Override
    public Cipher getCipher() {
        return keyAlgorithm.getCipher();
    }

    private PublicKey getPublicKeyFromBase64String(String keyString) {

        final String publicKeyString =
                keyString.replaceAll("\\n",  "").replaceAll("-{5}[ a-zA-Z]*-{5}", "");
        X509EncodedKeySpec keySpecX509 =
                new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyString));

        PublicKey publicKey = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpecX509);
        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }

        return publicKey;
    }
}
