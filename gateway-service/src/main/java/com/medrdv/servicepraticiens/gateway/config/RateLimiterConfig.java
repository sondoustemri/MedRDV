package com.medrdv.servicepraticiens.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    // 1. Le bean qui gère le compteur dans Redis (5 req/s, burst 10)
    @Bean
    @Primary
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(5, 10, 1);
    }

    // 2. Le bean qui identifie l'utilisateur (par IP)
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String ip = "unknown";
            if (exchange.getRequest().getRemoteAddress() != null
                    && exchange.getRequest().getRemoteAddress().getAddress() != null) {
                ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }
            return Mono.just(ip);
        };
    }

    // 3. La route définie en Java avec le filtre attaché
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("service-rendezvous-route", r -> r
                        .path("/service-rendezvous/**")
                        .filters(f -> f
                                .stripPrefix(1) // <-- Retire "/service-rendezvous" avant de transmettre
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                        )
                        .uri("lb://service-rendezvous")
                )
                .build();
    }
}