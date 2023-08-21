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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenProvider tokenProvider,
                                           UserServiceProxy userServiceProxy, ObjectMapper objectMapper) throws Exception {
        HttpSecurity httpSecurity = http
                .cors(withDefaults())
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin("http://localhost:63342");
        configuration.addAllowedOrigin("http://www.teamcloudnine.link");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
