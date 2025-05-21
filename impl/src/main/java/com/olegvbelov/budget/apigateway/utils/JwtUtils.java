package com.olegvbelov.budget.apigateway.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    private Algorithm algorithm;

    @PostConstruct
    public void init(){
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String getUserIdFromToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new JWTVerificationException("Token is null or empty");
            }
            var verifier = JWT.require(algorithm)
                    .withIssuer("https://dev-qwso2fflick7w32j.us.auth0.com/")
                    .build();
            var decodedJWT = verifier.verify(token.substring(7));
            return decodedJWT.getClaim("sub").asString().substring(6);
        } catch (JWTVerificationException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private boolean isTokenExpired(String token) {
        return true;
    }

    public boolean isInvalid(String token) {
        return this.isTokenExpired(token);
    }
}
