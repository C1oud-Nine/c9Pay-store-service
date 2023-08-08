package com.c9pay.storeservice.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.c9pay.storeservice.proxy.UserServiceProxy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;

import static com.c9pay.storeservice.constant.CookieConstant.AUTHORIZATION_HEADER;
import static com.c9pay.storeservice.jwt.TokenProvider.IP_ADDR;
import static com.c9pay.storeservice.jwt.TokenProvider.SERVICE_TYPE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class JwtFilterTest {
    static final String secret = "REVGQVVMVERFRkFVTFRERUZBVUxUREVGQVVMVERFRkFVTFRERUZBVUxUREVGQVVMVERFRkFVTFRERUZBVUxUREVGQVVMVA==";
    static final byte[] keyBytes = Base64.getDecoder().decode(secret);
    static final Algorithm algorithm = Algorithm.HMAC512(keyBytes);
    static MockedStatic<SecurityContextHolder> mSecurityContextHolder;
    MSecurityContext securityContext = mock(MSecurityContext.class);
    MRequest request = spy(MRequest.class);

    @BeforeAll
    static void setStaticMethod() {
        mSecurityContextHolder = mockStatic(SecurityContextHolder.class);
    }

    @BeforeEach
    void setup() {
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        doCallRealMethod().when(securityContext).setAuthentication(any());
        doCallRealMethod().when(securityContext).getAuthentication();
    }

    @Test
    void 가게토큰테스트() throws Exception {
        // given
        TokenProvider tokenProvider = new TokenProvider(secret, "store", 86400);
        String ip = "mock-ip";
        String storeSub = "1";
        String storeToken = getStoreToken(storeSub, ip);
        request.setToken(storeToken);
        request.setIp(ip);
        JwtFilter jwtFilter = new JwtFilter(tokenProvider, mock(TestUserServiceProxy.class));

        // when
        jwtFilter.doFilter(request, mock(MResponse.class), mock(FilterChain.class));

        // then
        Object name = securityContext.getAuthentication().getName();
        Collection<? extends GrantedAuthority> authorities = securityContext.getAuthentication().getAuthorities();
        Assertions.assertEquals(storeSub, name);
        Assertions.assertTrue(authorities.stream().map(GrantedAuthority::getAuthority).anyMatch("store"::equals));
    }

    @Test
    void 사용자토큰테스트() throws ServletException, IOException {
        // given
        TokenProvider tokenProvider = new TokenProvider(secret, "store", 86400);
        String userSub = "id";
        String userToken = getUserToken(userSub);
        UUID expected = UUID.randomUUID();
        TestUserServiceProxy userServiceProxy = new TestUserServiceProxy(expected);
        request.setToken(userToken);
        JwtFilter jwtFilter = new JwtFilter(tokenProvider, userServiceProxy);

        // when
        jwtFilter.doFilter(request, mock(MResponse.class), mock(FilterChain.class));

        // then
        Object name = securityContext.getAuthentication().getName();
        Collection<? extends GrantedAuthority> authorities = securityContext.getAuthentication().getAuthorities();
        Assertions.assertEquals(expected.toString(), name);
        Assertions.assertTrue(authorities.stream().map(GrantedAuthority::getAuthority).anyMatch("user"::equals));
    }

    abstract static class MSecurityContext implements SecurityContext {
        Authentication authentication;

        @Override
        public Authentication getAuthentication() {
            return authentication;
        }

        @Override
        public void setAuthentication(Authentication authentication) {
            this.authentication = authentication;
        }
    }

    abstract static class MRequest implements HttpServletRequest {
        String ip;
        String token;
        @Override
        public Cookie[] getCookies() {
            Cookie cookie = new Cookie(AUTHORIZATION_HEADER, "Bearer+" + token);
            return new Cookie[]{ cookie };
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setToken(String token) {
            this.token = token;
        }

        @Override
        public String getRemoteAddr() {
            return ip;
        }
    }

    abstract static class MResponse implements HttpServletResponse {}

    private String getStoreToken(String sub, String ipAddr) {
        return JWT.create()
                .withSubject(sub)
                .withClaim(SERVICE_TYPE, "store")
                .withClaim(IP_ADDR, ipAddr)
                .sign(algorithm);
    }

    private String getUserToken(String sub) {
        return JWT.create()
                .withSubject(sub)
                .withClaim(SERVICE_TYPE, "user")
                .sign(algorithm);
    }

    class TestUserServiceProxy implements UserServiceProxy {
        public UUID userSerialNumber;

        public TestUserServiceProxy(UUID userSerialNumber) {
            this.userSerialNumber = userSerialNumber;
        }

        @Override
        public ResponseEntity<?> getSerialNumber(String token) {
            return ResponseEntity.ok(userSerialNumber);
        }
    }

    @AfterAll
    static void turnOffStaticMethod() {
        mSecurityContextHolder.close();
    }
}