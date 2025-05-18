package com.olegvbelov.budget.apigateway.config;

import com.olegvbelov.budget.apigateway.filter.AuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RoutesConfig {
    private static final String KEY_VALUE_JSON_TEMPLATE = " \"%s\": \"%s\"";

    private final GatewayUriConfig gatewayUriConfig;
    private final AuthenticationFilter filter;

    @Bean
    public RouteLocator authorizationOauthTokenRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("token", accessTokenRequest())
                .route("refreshToken", refreshTokenRequest())
                .route("transactions-service", r -> r.path("/transactions/**")
                        .filters(f -> f.filter(filter))
                        .uri(gatewayUriConfig.getUri()))
                .build();
    }

    private Function<PredicateSpec, Buildable<Route>> accessTokenRequest() {
        return ps -> ps.path("/token").filters(gatewayFilterSpec ->
                        gatewayFilterSpec.modifyRequestBody(
                                        LinkedHashMap.class,
                                        String.class,
                                        (exchange, request) -> Mono.just(buildTokenRequest(request, "password"))
                                )
                                .removeRequestHeader(HttpHeaders.ORIGIN)
                                .rewritePath("/token", "/executor/v1/auth/token")
                )
                .uri(gatewayUriConfig.getUri());
    }

    private Function<PredicateSpec, Buildable<Route>> refreshTokenRequest() {
        return ps -> ps.path("/token/refresh")
                .filters(gatewayFilterSpec -> gatewayFilterSpec.modifyRequestBody(
                                        LinkedHashMap.class,
                                        String.class,
                                        (exchange, request) -> Mono.just(buildTokenRequest(request, "refresh_token"))
                                )
                                .removeRequestHeader(HttpHeaders.ORIGIN)
                                .rewritePath("/token/refresh", "/executor/v1/auth/token")
                ).uri(gatewayUriConfig.getUri());
    }

    @SuppressWarnings({"rawtypes"})
    private String buildTokenRequest(LinkedHashMap request, String grantType) {
        var grantTypeRequestJson = String.format(KEY_VALUE_JSON_TEMPLATE, "grant_type", grantType);
        return String.format("{%s, %s}", buildJsonRequest(request), grantTypeRequestJson);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String buildJsonRequest(LinkedHashMap request) {
        return ((Map<String, Object>) request).entrySet()
                .stream()
                .map(this::mapToJsonString)
                .reduce((p1, p2) -> String.format("%s,%s", p1, p2))
                .orElse("");
    }

    @SuppressWarnings({"rawtypes"})
    private String mapToJsonString(Map.Entry<String, Object> mapEntry) {
        if (mapEntry.getValue() instanceof LinkedHashMap) {
            var value = buildJsonRequest((LinkedHashMap) mapEntry.getValue());
            return String.format(" \"%s\": { %s }", mapEntry.getKey(), value);
        }
        return String.format(KEY_VALUE_JSON_TEMPLATE, mapEntry.getKey(), mapEntry.getValue());
    }
}
