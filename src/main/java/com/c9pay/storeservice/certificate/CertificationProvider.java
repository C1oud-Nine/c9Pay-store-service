package com.c9pay.storeservice.certificate;

import com.c9pay.storeservice.data.dto.certificate.ServiceDetails;
import com.c9pay.storeservice.data.dto.certificate.CertificateResponse;
import com.c9pay.storeservice.data.dto.certificate.PublicKeyResponse;
import com.c9pay.storeservice.proxy.AuthServiceProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class CertificationProvider {
    private final AuthServiceProxy authServiceProxy;
    private final KeyAlgorithm keyAlgorithm;
    private final ObjectMapper objectMapper;
    private Optional<PublicKey> publicKey;

    @PostConstruct
    public void getPublicKey() {
        Optional<PublicKeyResponse> responseOptional = Optional.ofNullable(authServiceProxy.getPublicKey().getBody());
        publicKey = responseOptional.map(PublicKeyResponse::getPublicKey)
                .map(this::getPublicKeyFromBase64String);
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

    public Optional<ServiceDetails> decrypt(CertificateResponse encoded) {
        Cipher cipher = keyAlgorithm.getCipher();
        Signature signature = keyAlgorithm.getSignature();

        try {
            PublicKey pubKey = publicKey.orElseThrow(IllegalStateException::new);

            // Base64로 인코딩된 인증서 디코딩
            byte[] messageEncoded = Base64.getDecoder().decode(encoded.getCertificate().getBytes(StandardCharsets.UTF_8));

            // 서명 검증
            signature.initVerify(pubKey);
            signature.update(messageEncoded);
            byte[] sign = Base64.getDecoder().decode(encoded.getSign());

            if (signature.verify(sign)) {

                cipher.init(Cipher.DECRYPT_MODE, pubKey);

                String json = new String(cipher.doFinal(messageEncoded), StandardCharsets.UTF_8);
                ServiceDetails certificate = objectMapper.readValue(json, ServiceDetails.class);
                return Optional.of(certificate);

            } else {
                return Optional.empty();
            }
        } catch(IllegalStateException e){
            log.error("Auth-service의 PublicKey를 획득할 수 없습니다.");
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
