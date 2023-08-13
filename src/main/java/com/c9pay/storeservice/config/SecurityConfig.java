package com.c9pay.storeservice.config;

import com.c9pay.storeservice.jwt.JwtSecurityConfig;
import com.c9pay.storeservice.jwt.TokenProvider;
import com.c9pay.storeservice.proxy.UserServiceProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenProvider tokenProvider,
                                           UserServiceProxy userServiceProxy, ObjectMapper objectMapper) throws Exception {
        HttpSecurity httpSecurity = http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((s) -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((t) -> t.requestMatchers("/**").permitAll());

        httpSecurity.apply(new JwtSecurityConfig(tokenProvider, userServiceProxy, objectMapper));

        return httpSecurity.build();
    }

    @Bean
    public TokenProvider tokenProvider(@Value("${jwt.secret}") String secret,
                                       @Value("%{jwt.service-type}") String serviceType,
                                       @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
        return new TokenProvider(secret, serviceType, tokenValidityInSeconds);
    }
}
