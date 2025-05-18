package com.olegvbelov.budget.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

public class WebServerConfiguration implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
    @Value("${server.max-initial-line-length:65536}")
    private int maxInitialLineLength;

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        factory.addServerCustomizers(httpServer ->
                httpServer.httpRequestDecoder(spec -> spec.maxInitialLineLength(maxInitialLineLength))
        );
    }
}
