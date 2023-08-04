package com.c9pay.storeservice.jwt;

import com.auth0.jwt.interfaces.Claim;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
public class JwtData {
    private String sub;
    private Map<String, Claim> claims;
}
