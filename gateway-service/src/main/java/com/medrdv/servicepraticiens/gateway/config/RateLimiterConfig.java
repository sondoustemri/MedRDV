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
                .route("service-rendezvous-route", r -> r
                        .order(-1) // <-- FORCE LA PRIORITÉ SUR LES ROUTES AUTOMATIQUES
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
                .route("service-patients-route", r -> r
                        .order(-1)
                        .path("/service-patients/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://service-patients")
                )
                .route("service-praticiens-route", r -> r
                        .order(-1)
                        .path("/service-praticiens/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://service-praticiens")
                )
                .route("service-notification-route", r -> r
                        .order(-1)
                        .path("/service-notification/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://service-notification")
                )
                .build();
    }
}