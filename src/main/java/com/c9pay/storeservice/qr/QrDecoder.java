package com.c9pay.storeservice.qr;

import com.c9pay.storeservice.data.dto.auth.PublicKeyResponse;
import com.c9pay.storeservice.data.dto.qr.ExchangeToken;
import com.c9pay.storeservice.exception.NotExistException;
import com.c9pay.storeservice.proxy.AuthServiceProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Component
public class QrDecoder {
    private Optional<PublicKey> publicKeyOptional;
    private final AuthServiceProxy authServiceProxy;
    private final ObjectMapper objectMapper;

    public QrDecoder(AuthServiceProxy authServiceProxy, ObjectMapper objectMapper) {
        this.authServiceProxy = authServiceProxy;
        this.objectMapper = objectMapper;
        updatePublicKey();
    }

    public Optional<UUID> getSerialNumber(ExchangeToken exchangeToken) {
        if (publicKeyOptional.isEmpty()) updatePublicKey();

        PublicKey publicKey = publicKeyOptional.orElseThrow(()->new NotExistException("인증 서비스의 응답이 없습니다."));
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(publicKey);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            byte[] encrypted = Base64.getDecoder().decode(exchangeToken.getContent().getBytes());
            byte[] decrypted = cipher.doFinal(encrypted);
            String decoded = new String(decrypted);

            String verifyString = exchangeToken.getContent() + exchangeToken.getExpiredAt();
            signature.update(verifyString.getBytes());
            byte[] sign = Base64.getDecoder().decode(exchangeToken.getSign().getBytes());

            if (signature.verify(sign)) {
                return Optional.of(UUID.fromString(decoded));
            } else return Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updatePublicKey() {
        publicKeyOptional = Optional.ofNullable(authServiceProxy.getPublicKey().getBody())
                .map(PublicKeyResponse::getPublicKey)
                .map(this::stringToPublicKey);
    }

    private PublicKey stringToPublicKey(String publicKeyString) {
        try {
            System.out.println(publicKeyString);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] bytePublicKey = Base64.getDecoder().decode(publicKeyString.getBytes());
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bytePublicKey);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return publicKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
