package net.vrabie.gatewaytake001;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class GatewayTake001Application {

    public static void main(String[] args) {
        SpringApplication.run(GatewayTake001Application.class, args);
    }

    @Bean
    RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(4, 7);
    }

    @Bean
    RouteLocator getRoutes(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(routeSpec -> routeSpec
                        .path("/proxy")
                        .filters(filterSpec -> filterSpec
                                .setPath("/reservations")
                                .addResponseHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                                .requestRateLimiter(rl -> rl
                                        .setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(new KeyResolver() {
                                            @Override
                                            public Mono<String> resolve(ServerWebExchange exchange) {
                                                Mono<String> name = exchange.getPrincipal()
                                                        .map(principal -> principal.getName());
                                                return name
                                                        .switchIfEmpty(Mono.just("Inna"));
                                            }
                                        })))

                        .uri("http://localhost:8080"))
                .build();
    }

}
