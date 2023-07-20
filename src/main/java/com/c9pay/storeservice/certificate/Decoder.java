package com.c9pay.storeservice.certificate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class Decoder {
    private final ObjectMapper objectMapper;

    public <T> Optional<T> decrypt(PublicKeyProvider publicKeyProvider, String content, String sign, Class<T> clazz) {
        Cipher cipher = publicKeyProvider.getCipher();
        Signature signature = publicKeyProvider.getSignature();
        try {
            PublicKey publicKey = publicKeyProvider.getPublicKey();

            // Base64로 인코딩된 인증서 디코딩
            byte[] messageEncoded = Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8));

            // 서명 검증
            signature.initVerify(publicKey);
            signature.update(messageEncoded);

            if (signature.verify(Base64.getDecoder().decode(sign))) {

                cipher.init(Cipher.DECRYPT_MODE, publicKey);

                String json = new String(cipher.doFinal(messageEncoded), StandardCharsets.UTF_8);
                T obj = objectMapper.readValue(json, clazz);
                return Optional.of(obj);

            } else {
                return Optional.empty();
            }
        } catch(KeyAcquisitionFailureException e){
            log.error("{}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> decrypt(PublicKeyProvider publicKeyProvider, String content, Class<T> clazz) {
        Cipher cipher = publicKeyProvider.getCipher();
        try {
            PublicKey publicKey = publicKeyProvider.getPublicKey();

            // Base64로 인코딩된 인증서 디코딩
            byte[] messageEncoded = Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            String json = new String(cipher.doFinal(messageEncoded), StandardCharsets.UTF_8);
            T obj = objectMapper.readValue(json, clazz);
            return Optional.of(obj);

        } catch(KeyAcquisitionFailureException e){
            log.error("{}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
