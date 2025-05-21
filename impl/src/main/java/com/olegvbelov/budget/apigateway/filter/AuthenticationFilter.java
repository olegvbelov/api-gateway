package com.olegvbelov.budget.apigateway.filter;

import com.olegvbelov.budget.apigateway.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RefreshScope
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter {

    private final JwtUtils jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (isAuthMissing(request)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
        var token = getAuthHeader(request);
        var userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
        return chain.filter(exchange);
    }

    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(0);
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private void updateRequest(ServerWebExchange exchange, String userId) {
        exchange.getRequest().mutate()
                .header("userId", String.valueOf(userId))
                .build();
    }
}
