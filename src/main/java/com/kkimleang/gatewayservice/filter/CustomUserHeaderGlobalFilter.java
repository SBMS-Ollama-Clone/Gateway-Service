package com.kkimleang.gatewayservice.filter;


import com.kkimleang.gatewayservice.dto.Response;
import com.kkimleang.gatewayservice.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CustomUserHeaderGlobalFilter implements GatewayFilter {
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final WebClient webClient;

    @Value("${service.auth.verify_user_mapping}")
    private String verifyUserMapping;

    public CustomUserHeaderGlobalFilter(WebClient webClient) {
        this.webClient = webClient;
    }

    public boolean isAnonymousPath(String requestPath) {
        String[] freeURLS = {
                "/api/auth/login",
                "/api/auth/signup",
                "/api/auth/verify",
                "/api/auth/user/me",
                "/api/token/**",
                "/oauth2/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/api-docs/**",
                "/aggregate/**",
                "/actuator/**",
        };
        for (String pattern : freeURLS) {
            if (pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!isAnonymousPath(exchange.getRequest().getURI().getPath())) {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Mono.error(new RuntimeException("missing or invalid Authorization header"));
            }
            String token = authHeader.substring(7);
            Mono<Response> userResponse = webClient
                    .get()
                    .uri(verifyUserMapping)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(Response.class);
            return userResponse.flatMap(userRes -> {
                UserResponse user = userRes.getPayload();
                log.info("User-Id: {}", user.getId());
                if (user.getId() == null) {
                    return Mono.error(new RuntimeException("Invalid user!"));
                }
//                Optional.ofNullable(tracer.currentTraceContext().context()).ifPresent(context -> {
//                    exchange.getRequest().mutate().header("Trace-Id", context.traceId()).build();
//                    exchange.getRequest().mutate().header("Span-Id", context.spanId()).build();
//                });
                exchange.getRequest().mutate().header("User-Id", user.getId().toString()).build();
                exchange.getRequest().mutate().header("Username", user.getUsername()).build();
                return chain.filter(exchange);
            });
        }
        return chain.filter(exchange);
    }

}

