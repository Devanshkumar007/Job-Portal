package com.capg.ApiGateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/job-service/v3/api-docs",
                                "/auth-service/v3/api-docs",
                                "/application-service/v3/api-docs",
                                "/admin-service/v3/api-docs"
                        ).permitAll()

                        // ✅ Auth APIs (ALLOW)
                        .pathMatchers("/api/auth/**").permitAll()
                        .anyExchange().permitAll()
                )

                .build();
    }
}
