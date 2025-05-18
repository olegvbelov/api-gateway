package com.olegvbelov.budget.apigateway.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String getAllClaimsFromToken(String token) {
        var payload = Jwts.parser().build()
                .parse(token);
        return (String) payload.toString();
    }

    private boolean isTokenExpired(String token) {
        return true;
    }

    public boolean isInvalid(String token) {
        return this.isTokenExpired(token);
    }
}
