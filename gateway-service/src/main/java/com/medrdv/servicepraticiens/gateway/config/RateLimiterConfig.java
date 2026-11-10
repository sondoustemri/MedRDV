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

    @Bean
    @Primary
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(5, 10, 1);
    }

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

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 1. Route pour service-rendezvous (avec Rate Limiter)
                .route("service-rendezvous-route", r -> r
                        .path("/service-rendezvous/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver())
                                )
                        )
                        .uri("lb://service-rendezvous")
                )
                // 2. Route pour service-patients
                .route("service-patients-route", r -> r
                        .path("/service-patients/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://service-patients")
                )
                // 3. Route pour service-praticiens
                .route("service-praticiens-route", r -> r
                        .path("/service-praticiens/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://service-praticiens")
                )
                // 4. Route pour service-notification (optionnel)
                .route("service-notification-route", r -> r
                        .path("/service-notification/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://service-notification")
                )
                .build();
    }
}