package com.voyagia.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Health Check route
                        .requestMatchers("/health/**").permitAll()

                        // Actuator route
                        .requestMatchers("/actuator/**").permitAll()

                        // Test API route
                        .requestMatchers("/test/**").permitAll()

                        // Authentication api(JWT testing) route
                        .requestMatchers("/auth/**").permitAll()

                        // Swagger/API route
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}