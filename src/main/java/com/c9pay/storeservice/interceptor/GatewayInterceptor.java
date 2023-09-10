package com.c9pay.storeservice.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.c9pay.storeservice.constant.GatewayConstant.API;


@Slf4j
public class GatewayInterceptor implements HandlerInterceptor {

    @Value("${symmetric.key}")
    private String key;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod handlerMethod){
            GatewayValidation gatewayValidation = handlerMethod.getMethodAnnotation(GatewayValidation.class);
            String header = request.getHeader(API);
            if(gatewayValidation == null) return true;
            else if(header != null && header.equals(key)){
                String headerName = gatewayValidation.value();
                String headerValue = request.getHeader(headerName);
                return headerValue.equals(key);
            }
            else{
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Unauthorized access.(Gateway)");
                return false;
            }
        }
        return true;
    }

}
