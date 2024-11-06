package com.kkimleang.gatewayservice.config;

import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class WebClientConfiguration {
    @Value("${service.auth.http}")
    private String authServiceEndpoint;

    @LoadBalanced
    @Bean
    public WebClient webClient() {
        log.info("Auth-Service HTTP endpoint: {}", authServiceEndpoint);
        return WebClient.builder()
                .baseUrl(authServiceEndpoint)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

