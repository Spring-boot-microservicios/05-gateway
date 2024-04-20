package com.angelfg.gateway.beans;

import com.angelfg.gateway.filters.AuthFilter;
import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Set;

@Configuration
@AllArgsConstructor
public class GatewayBean {

    private final AuthFilter authFilter;

    // Configuracion de microservicios
//    @Bean
//    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
//        return builder
//                .routes()
//                .route(route -> route
//                        .path("/companies-crud/company/*")
//                        .uri("http://localhost:8081")
//                )
//                .route(route -> route
//                        .path("/report-ms/report/*")
//                        .uri("http://localhost:7070")
//                ).build();
//    }

    @Bean
    @Profile(value = "eureka-off")
    public RouteLocator routeLocatorEurekaOff(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(route -> route
                        .path("/companies-crud/company/**")
                        .uri("http://localhost:8081")
                )
                .route(route -> route
                        .path("/report-ms/report/**")
                        .uri("http://localhost:7070")
                ).build();
    }

    @Bean
    @Profile(value = "eureka-on")
    public RouteLocator routeLocatorEurekaOn(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(route -> route
                        .path("/companies-crud/company/**")
                        .uri("lb://companies-crud")
                )
                .route(route -> route
                        .path("/report-ms/report/**")
                        .uri("lb://report-ms")
                ).build();
    }

    @Bean
    @Profile(value = "eureka-on-circuit-breaker")
    public RouteLocator routeLocatorEurekaOnCircuitBreaker(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(route -> route
                        .path("/companies-crud/company/**")
                        .filters(filter -> {
                            filter.circuitBreaker(config -> config
                                    .setName("gateway-circuit-breaker")
                                    .setStatusCodes(Set.of("500", "400"))
                                    .setFallbackUri("forward:/companies-crud-fallback/company/*")
                            );

                            return filter;
                        })
                        .uri("lb://companies-crud")
                )
                .route(route -> route
                        .path("/report-ms/report/**")
                        .uri("lb://report-ms")
                )
                .route(route -> route
                        .path("/companies-crud-fallback/company/**") // nombre de context path
                        .uri("lb://companies-crud-fallback") // nombre de microservicio
                )
                .build();
    }


    @Bean
    @Profile(value = "oauth2")
    public RouteLocator routeLocatorEOauth2(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(route -> route
                        .path("/companies-crud/company/**")
                        .filters(filter -> {
                            filter.circuitBreaker(config -> config
                                    .setName("gateway-circuit-breaker")
                                    .setStatusCodes(Set.of("500", "400"))
                                    .setFallbackUri("forward:/companies-crud-fallback/company/*")
                            );

                            filter.filter(this.authFilter);

                            return filter;
                        })
                        .uri("lb://companies-crud")
                )
                .route(route -> route
                        .path("/report-ms/report/**")
                        .filters(filter -> filter.filter(this.authFilter))
                        .uri("lb://report-ms")
                )
                .route(route -> route
                        .path("/companies-crud-fallback/company/**") // nombre de context path
                        .filters(filter -> filter.filter(this.authFilter))
                        .uri("lb://companies-crud-fallback") // nombre de microservicio
                )
                .route(route -> route
                        .path("/auth-server/auth/**") // nombre de context path
                        .uri("lb://auth-server") // nombre de microservicio
                )
                .build();
    }

}
