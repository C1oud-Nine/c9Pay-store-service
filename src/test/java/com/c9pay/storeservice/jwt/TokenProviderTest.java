package com.c9pay.storeservice.jwt;

import com.c9pay.storeservice.jwt.TokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class TokenProviderTest {
    final String secretKey = "REVGQVVMVERFRkFVTFRERUZBVUxUREVGQVVMVERFRkFVTFRERUZBVUxUREVGQVVMVERFRkFVTFRERUZBVUxUREVGQVVMVA==";
    final String serviceType = "user";
    @Test
    void 토큰생성테스트() {
        // given
        Authentication auth = mock();
        given(auth.getName()).willReturn("jaedoo");
        long tokenValidityInSeconds = 24*60*60;
        TokenProvider tokenProvider = new TokenProvider(secretKey, serviceType, tokenValidityInSeconds);

        // when
        String token = tokenProvider.createToken(auth, "1234");
        System.out.println(token);
        Authentication authentication = tokenProvider.getAuthentication(token);

        // then
        assertEquals("jaedoo", authentication.getName());
        assertIterableEquals(List.of(new SimpleGrantedAuthority("store")), authentication.getAuthorities());
    }

    @Test
    void 토큰만료테스트() throws InterruptedException {
        // given
        Authentication auth = mock();
        given(auth.getName()).willReturn("jaedoo");
        long tokenValidityInSeconds = 1;
        TokenProvider tokenProvider = new TokenProvider(secretKey, serviceType, tokenValidityInSeconds);

        String token = tokenProvider.createToken(auth, "1234");
        System.out.println(token);

        // when
        sleep(1500);
        boolean isValid = tokenProvider.validateToken(token, mock());

        // then
        assertFalse(isValid);
    }

    @Test
    void 시그니처가_다른_토큰_디코딩() {
        // given
        String userSecret = "ABCD" + secretKey.substring(4);

        Authentication storeAuth = mock();
        Authentication userAuth = mock();
        given(storeAuth.getName()).willReturn("JD");
        given(userAuth.getName()).willReturn("jaedoo");
        long tokenValidityInSeconds = 60*60*24;
        TokenProvider storeTokenProvider = new TokenProvider(secretKey, serviceType, tokenValidityInSeconds);
        TokenProvider userTokenProvider = new TokenProvider(userSecret, "user", tokenValidityInSeconds);

        String storeToken = storeTokenProvider.createToken(storeAuth, "1234");
        String userToken = userTokenProvider.createToken(userAuth, "1234");
        System.out.println("store: " + storeToken);
        System.out.println("user: " + userToken);

        // when
        Authentication authentication = storeTokenProvider.getAuthentication(userToken);

        // then
        assertEquals("jaedoo", authentication.getName());
        assertIterableEquals(List.of(new SimpleGrantedAuthority("user")), authentication.getAuthorities());
    }
}