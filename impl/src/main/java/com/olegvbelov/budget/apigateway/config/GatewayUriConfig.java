package com.olegvbelov.budget.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "gateway")
public class GatewayUriConfig {
    private String uri;
}
