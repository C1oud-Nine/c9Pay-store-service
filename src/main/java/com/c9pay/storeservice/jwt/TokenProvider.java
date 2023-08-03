package com.c9pay.storeservice.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Slf4j
public class TokenProvider {
    private final String serviceType;
    private final long tokenValidityInSeconds;
    private final String SERVICE_TYPE = "type";
    private final String IP_ADDR = "ip";
    private Algorithm algorithm;

    public TokenProvider(@Value("${jwt.secret}") String secret,
                         @Value("%{jwt.service-type}") String serviceType,
                         @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
        this.serviceType = serviceType;
        this.tokenValidityInSeconds = tokenValidityInSeconds;
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.algorithm = Algorithm.HMAC512(keyBytes);
    }

    public String createToken(Authentication authentication, String ipAddr) {
        long now = System.currentTimeMillis();

        String token = JWT.create()
                .withSubject(authentication.getName())
                .withClaim(SERVICE_TYPE, serviceType)
                .withClaim(IP_ADDR, ipAddr)
                .withExpiresAt(Instant.ofEpochMilli(System.currentTimeMillis() + tokenValidityInSeconds * 1000))
                .sign(algorithm);

        log.debug("생성된 토큰: {}", token);

        return token;
    }

    public Authentication getAuthentication(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        String sub = decodedJWT.getSubject();
        String type = decodedJWT.getClaim(SERVICE_TYPE).as(String.class);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(type));
        User principal = new User(sub, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token, HttpServletRequest request) {
        try {
            JWTVerifier.BaseVerification verification = (JWTVerifier.BaseVerification) JWT.require(algorithm)
                    .withClaimPresence(SERVICE_TYPE)
                    .acceptExpiresAt(tokenValidityInSeconds);

            JWTVerifier verifier = verification.build(Clock.systemUTC());

            DecodedJWT decodedJWT = verifier.verify(token);
            // todo : ipaddr로 구분 필요
//            if (decodedJWT.getClaim(SERVICE_TYPE).as(String.class).equals(serviceType)) {
//                String ipAddr = decodedJWT.getClaim(IP_ADDR).as(String.class);
//                if (!ipAddr.equals(request.getRemoteAddr())) {
//                    log.debug("Store token ip is different. expected: {}, actual: {}", ipAddr, request.getRemoteAddr());
//                    return false;
//                }
//            }

            return true;

        } catch (JWTVerificationException e) {
            log.debug("auth fail reason: {}", e.getMessage());
            return false;
        }
    }
}
