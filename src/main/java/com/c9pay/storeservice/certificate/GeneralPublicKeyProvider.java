package com.c9pay.storeservice.certificate;

import com.c9pay.storeservice.proxy.AuthServiceProxy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
public class GeneralPublicKeyProvider implements PublicKeyProvider {
    private final KeyAlgorithm keyAlgorithm;
    private final String keyString;

    @Override
    public PublicKey getPublicKey() throws KeyAcquisitionFailureException {
        return getPublicKeyFromBase64String(keyString);
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

    @Override
    public Signature getSignature() {
        return keyAlgorithm.getSignature();
    }

    @Override
    public Cipher getCipher() {
        return keyAlgorithm.getCipher();
    }
}
