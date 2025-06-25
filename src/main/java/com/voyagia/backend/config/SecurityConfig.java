package com.voyagia.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Settings
 * <p>
 * - Web security
 * - Password encode
 * - Authentication/authorization
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * HTTP
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     * @throws Exception 설정 중 발생하는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disabled ( API server )
                .csrf(csrf -> csrf.disable())

                // 요청별 인증 설정
                .authorizeHttpRequests(auth -> auth
                        // Health Check route - No authentication required
                        .requestMatchers("/health/**").permitAll()

                        // Actuator route - No authentication required (development env)
                        .requestMatchers("/actuator/**").permitAll()

                        // Test API route - No authentication required (development env)
                        .requestMatchers("/test/**").permitAll()

                        // Authentication api(JWT testing) route - No authentication required
                        // (login/signup)
                        .requestMatchers("/auth/**").permitAll()

                        // User registration route - No authentication required (signup)
                        .requestMatchers("/users/register").permitAll()

                        // Swagger/API route - No authentication required (development env)
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        .anyRequest().authenticated());

        return http.build();
    }

    /**
     * Using BCryptPasswordEncoder
     * - Strong hash algorithms
     * - Automated salt
     * - Slow speed to defend against brute force attacks
     *
     * @return PasswordEncoder Implement
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}