package com.c9pay.storeservice.jwt;

import com.c9pay.storeservice.proxy.UserServiceProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.c9pay.storeservice.constant.CookieConstant.AUTHORIZATION_HEADER;
import static com.c9pay.storeservice.jwt.TokenProvider.IP_ADDR;
import static com.c9pay.storeservice.jwt.TokenProvider.SERVICE_TYPE;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter implements Filter {
    private final TokenProvider tokenProvider;
    private final UserServiceProxy userServiceProxy;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String jwt = resolveToken(httpServletRequest);
        String ipAddress = httpServletRequest.getRemoteAddr();

        if (StringUtils.hasText(jwt)) {
            JwtData jwtData = tokenProvider.getJwtData(jwt);

            boolean isUserToken =  jwtData.getClaims().containsKey(SERVICE_TYPE) &&
                    "user".equals(jwtData.getClaims().get(SERVICE_TYPE).asString());

            boolean isStoreTokenValid = jwtData.getClaims().containsKey(SERVICE_TYPE) &&
                    "store".equals(jwtData.getClaims().get(SERVICE_TYPE).asString()) &&
                    tokenProvider.validateToken(jwt) &&
                    jwtData.getClaims().containsKey(IP_ADDR) &&
                    ipAddress.equals(jwtData.getClaims().get(IP_ADDR).asString());

            if (isStoreTokenValid) {
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            if (isUserToken) {
                Optional<UUID> userSerialNumber = getUserSerialNumber(httpServletRequest);
                if (userSerialNumber.isPresent()) {
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("user");
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userSerialNumber.get(), null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) throws UnsupportedEncodingException {
        String bearerToken = null;
        if (request.getCookies() != null)
            bearerToken = Arrays.stream(request.getCookies()).filter((cookie -> cookie.getName().equals(AUTHORIZATION_HEADER)))
                    .findFirst().map(Cookie::getValue).orElse(null);

        log.debug("쿠키의 토큰 : {}", bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer+")) {
            bearerToken = URLDecoder.decode(bearerToken, "utf-8");
            log.debug("헤더의 토큰은 {}", bearerToken);
            String token = bearerToken.substring(7);
            log.debug("jwt 토큰은 {}", token);
            return token;
        }

        return null;
    }

    private Optional<UUID> getUserSerialNumber(HttpServletRequest request) {
        String bearerToken = null;
        if (request.getCookies() != null)
            bearerToken = Arrays.stream(request.getCookies()).filter((cookie -> cookie.getName().equals(AUTHORIZATION_HEADER)))
                    .findFirst().map(Cookie::getValue).orElse(null);

        if (StringUtils.hasText(bearerToken)) {
            ResponseEntity<String> serialNumberResponse = userServiceProxy.getSerialNumber(bearerToken);

            if (serialNumberResponse.getStatusCode().is2xxSuccessful()) {

                return Optional.ofNullable(serialNumberResponse.getBody())
                        .map((uuid)->UUID.fromString(uuid));
            }
        }

        return Optional.empty();
    }
}
