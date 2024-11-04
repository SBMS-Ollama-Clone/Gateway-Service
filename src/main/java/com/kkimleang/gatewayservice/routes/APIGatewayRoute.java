package com.kkimleang.gatewayservice.routes;

import com.kkimleang.gatewayservice.config.RateLimiterConfig;
import com.kkimleang.gatewayservice.filter.CustomUserHeaderGlobalFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.function.Function;

@RequiredArgsConstructor
@Configuration
public class APIGatewayRoute {
    private final CustomUserHeaderGlobalFilter customGlobalFilter;
    private final RateLimiterConfig rateLimiterConfig;

    @Value("${service.auth.lb}")
    private String authServiceLB;
    @Value("${service.chat.lb}")
    private String chatServiceLB;
    @Value("${service.setting.lb}")
    private String settingServiceLB;
    @Value("${service.content.lb}")
    private String contentServiceLB;

    @Value("${service.auth.docs}")
    private String authServiceSwagger;
    @Value("${service.chat.docs}")
    private String chatServiceSwagger;
    @Value("${service.content.docs}")
    private String contentServiceSwagger;
    @Value("${service.setting.docs}")
    private String settingServiceSwagger;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service-swagger", r -> r.path(authServiceSwagger)
                        .filters(f -> f.setPath("/api-docs"))
                        .uri(authServiceLB))
                .route("chat-service-swagger", r -> r.path(chatServiceSwagger)
                        .filters(f -> f.setPath("/api-docs"))
                        .uri(chatServiceLB))
                .route("content-service-swagger", r -> r.path(contentServiceSwagger)
                        .filters(f -> f.setPath("/api-docs"))
                        .uri(contentServiceLB))
                .route("setting-service-swagger", r -> r.path(settingServiceSwagger)
                        .filters(f -> f.setPath("/api-docs"))
                        .uri(settingServiceLB))

                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(brutalCorsFilter("/api/auth"))
                        .uri(authServiceLB))
                .route("oauth2-service", r -> r.path("/oauth2/**")
                        .filters(brutalCorsFilter("/api/oauth2"))
                        .uri(authServiceLB))
                .route("chat-service", r -> r.path("/api/chats/**")
                        .filters(brutalCorsFilter("/api/chats"))
                        .uri(chatServiceLB))
                .route("content-service", r -> r.path("/api/contents/**")
                        .filters(brutalCorsFilter("/api/contents"))
                        .uri(contentServiceLB))
                .route("setting-service", r -> r.path("/api/settings/**")
                        .filters(brutalCorsFilter("/api/settings"))
                        .uri(settingServiceLB))
                .build();
    }

    Function<GatewayFilterSpec, UriSpec> brutalCorsFilter(final String serviceName) {
        return f -> f
                .requestRateLimiter(config -> {
                    config.setRateLimiter(redisRateLimiter());
                    config.setKeyResolver(rateLimiterConfig.ipKeyResolver());
                })
                .filter(customGlobalFilter)
                .rewritePath("/" + serviceName + "/(?<segment>.*)", "/${segment}")
                .setResponseHeader("Access-Control-Allow-Origin", "*")
                .setResponseHeader("Access-Control-Allow-Methods", "*")
                .setResponseHeader("Access-Control-Expose-Headers", "*");
    }

    // RedisRateLimiter bean configuration
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20); // 10 requests per second, burst capacity of 20
    }
}
