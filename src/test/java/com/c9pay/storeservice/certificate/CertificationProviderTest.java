package com.c9pay.storeservice.certificate;

import com.c9pay.storeservice.data.dto.certificate.CertificateResponse;
import com.c9pay.storeservice.data.dto.certificate.PublicKeyResponse;
import com.c9pay.storeservice.data.dto.certificate.ServiceDetails;
import com.c9pay.storeservice.data.dto.certificate.ServiceInfo;
import com.c9pay.storeservice.proxy.AuthServiceProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.security.cert.Certificate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class CertificationProviderTest {
    CertificationProvider certificationProvider;
    static final KeyAlgorithm rsa = new Rsa();
    AuthServiceProxy authServiceProxy;
    static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnwYpbFadGxnYQdAdTfgKmJvzLTRqPrAelDN6FE9+" +
            "WMEoVyY5n1x00v8SBNVLrTEuZmqhrSMsh8V89mSIDPWFUcopcfnAP1/sHzExeg/Z15CKB2jbRsPkQ+mtS97aqxKnpilOeu/" +
            "9O4xsEHEuES0xD8mKqTphQ9ZVWAk/rbzKAV30Ga/DqpYWyBbsSkxjCblFR2MUG9z/ZS8O7UH02+tsZgcRcD1VFt/0Y+A11J" +
            "C0BZl8wcdUXNRto3ihXgzL8/K/kb463UJ+tMDLBfN6+3qgM1LZoilQbNT+X5R7ncZ4gzjEHfazu/pmfRsmu65BPVo/62+gQ" +
            "oPaIgeuiS0fz2ZbKQIDAQAB";

    static final String signature = "gHhS0DUMET7jw/xd/jATVBvcZMqh6nS9BioXsGfPOMCkZT1tt6BJIGLYerffYtoYGrnn42UY3oIqnCymWA0Z" +
            "KaElK63vLPXX4Czfyarh/zbZihL2vDUB8RAaTi/c0EipS4ZA4p5DhoAHeAKtmQ9D9J8hORuzdwse6TyUERPzsHACu1icmpT" +
            "KkVKbt51Y2OhNU49OtrPB3kCgbfZ2DYZ1D7um0u46XSM2cK9LIUgbW95+xHZv3tGTcTgMaYiNHNEsK4vRn12D/6R9J5ltsN" +
            "vttQY2i+4xSxzQx7qbSN95T3g/S31yNdGEi+cRUbucwcu1CUBYKAjtcbK4iq+Uacffvg==";

    static final String certification = "nqOGT2xC0uu4yQWDAch6x0m9n0RSWuOh3aah6zWGGftpsh2w7kgTlEtDKBOuUbOsCC2W0b" +
            "E43LK3Fh5QMSm9VuGyiahiTreHlK12cw1XzGmeRSz0K3r90+AuMOBGzvUWKrbeVVDXdI9A/T5wBGnKT3OHk3uZ/t75clJ+V" +
            "xUjM34R2LVnfo4XNL9wXp8rnbtxl+zwqYmgU4jqaQJp1Q0O12wu+wOsPJyPD0YD+diMJksmtIT/YBhwvEYTlyyAtWlQc4X4" +
            "zoh4We12ei5Fz2w40T3tQsZFlG/crFie/JhXEQ6NP5NZah+CRdHOYHNkYqzWM6aaXdDkZSHhsf30xzetxQ==";
    @BeforeEach
    void setUp() {
        authServiceProxy = mock(MockAuthServiceProxy.class);
        given(authServiceProxy.getPublicKey())
                .willReturn(ResponseEntity.ok(new PublicKeyResponse(publicKey)));
        certificationProvider = new CertificationProvider(authServiceProxy, rsa, new ObjectMapper());
        certificationProvider.getPublicKey();
    }

    @Test
    @DisplayName("CertificationProvider 복호화 테스트")
    void 복호화_테스트() {
        // given
        ServiceInfo expectedServiceInfo = new ServiceInfo("dummy-service", "/dummy");
        ServiceDetails expected = new ServiceDetails("dummy-public-key", expectedServiceInfo);
        CertificateResponse encoded = new CertificateResponse(certification, signature);

        // when
        Optional<ServiceDetails> decrypt = certificationProvider.decrypt(encoded);

        // then
        assertTrue(decrypt.isPresent());
        assertEquals(expected.toString(), decrypt.get().toString());
    }

    abstract static class MockAuthServiceProxy implements AuthServiceProxy {
    }
}