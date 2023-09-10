package com.c9pay.storeservice.config;

import com.c9pay.storeservice.interceptor.GatewayInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(customInterceptor()).addPathPatterns("/store-service/api/**");
    }
    @Bean
    public GatewayInterceptor customInterceptor(){
        return new GatewayInterceptor();
    }
}