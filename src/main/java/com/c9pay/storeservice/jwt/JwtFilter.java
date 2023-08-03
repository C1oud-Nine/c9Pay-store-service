package com.c9pay.storeservice.jwt;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.UUID;

import static com.c9pay.storeservice.constant.CookieConstant.AUTHORIZATION_HEADER;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter implements Filter {
    private final TokenProvider tokenProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String jwt = resolveToken(httpServletRequest);
        String ipAddress = httpServletRequest.getRemoteAddr();

        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt, httpServletRequest)) {
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("user"))) {
                // todo 사용자 서비스로부터 사용자식별번호 획득필요
                UUID userId = UUID.fromString("64cf334e-1b12-11ee-be56-0242ac120002");
                ((HttpServletRequest) request).getSession().setAttribute("userId", userId);
            } else {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) throws UnsupportedEncodingException {
        String bearerToken = null;
        if (request.getCookies() != null)
            bearerToken = Arrays.stream(request.getCookies()).filter((cookie -> cookie.getName().equals(AUTHORIZATION_HEADER)))
                    .findFirst().map(cookie -> cookie.getValue()).orElse(null);

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
}
