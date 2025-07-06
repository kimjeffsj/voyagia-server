package com.voyagia.backend.config;

import com.voyagia.backend.security.JwtAccessDeniedHandler;
import com.voyagia.backend.security.JwtAuthenticationEntryPoint;
import com.voyagia.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security Settings
 * <p>
 * - Web security
 * - Password encode
 * - Authentication/authorization
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
        private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

        // Constructor injection
        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                        JwtAccessDeniedHandler jwtAccessDeniedHandler) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
                this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        }

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
                                // CSRF 비활성화 (JWT 사용으로 불필요)
                                .csrf(csrf -> csrf.disable())

                                // CORS 설정 적용
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // 세션 관리 설정 (Stateless)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Exception Handling 설정
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                                .accessDeniedHandler(jwtAccessDeniedHandler))

                                // 요청별 인증 설정
                                .authorizeHttpRequests(auth -> auth
                                                // ============ Public 경로 (인증 불필요) ============

                                                // Health Check
                                                .requestMatchers("/health/**").permitAll()
                                                .requestMatchers("/actuator/**").permitAll()

                                                // API Documentation
                                                .requestMatchers("/swagger-ui/**").permitAll()
                                                .requestMatchers("/v3/api-docs/**").permitAll()
                                                .requestMatchers("/swagger-resources/**").permitAll()
                                                .requestMatchers("/webjars/**").permitAll()

                                                // Test endpoints (개발 환경)
                                                .requestMatchers("/test/**").permitAll()

                                                // ============ Authentication 경로 ============

                                                // 인증 관련 (로그인, 회원가입, 토큰 검증 등)
                                                .requestMatchers("/auth/**").permitAll()

                                                // 회원가입 관련
                                                .requestMatchers(HttpMethod.POST, "/users/register").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/users/check-email").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/users/check-username")
                                                .permitAll()

                                                // ============ User 경로 (인증 필요) ============

                                                // 사용자 프로필 조회/수정 (본인만)
                                                .requestMatchers(HttpMethod.GET, "/users/{id}").authenticated()
                                                .requestMatchers(HttpMethod.PUT, "/users/{id}").authenticated()
                                                .requestMatchers(HttpMethod.PUT, "/users/{id}/password")
                                                .authenticated()

                                                // 사용자 관리 (관리자만)
                                                .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/users/active").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/users/search").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/users/search/advanced")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/users/stats").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/users/{id}/activate")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/users/{id}/deactivate")
                                                .hasRole("ADMIN")

                                                // ============ Product 경로 ============

                                                // 상품 조회 (공개)
                                                .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()

                                                // 상품 관리 (관리자만)
                                                .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/categories/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/categories/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/categories/**")
                                                .hasRole("ADMIN")

                                                // ============ Order 및 Cart 경로 (인증 필요) ============

                                                .requestMatchers("/cart/**").authenticated()
                                                .requestMatchers("/orders/**").authenticated()

                                                // ============ 기타 모든 요청 (인증 필요) ============

                                                .anyRequest().authenticated())

                                // JWT 인증 필터 추가
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * CORS settings
         *
         * @return CORS setting source
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // 허용할 출처 (개발 환경)
                configuration.setAllowedOriginPatterns(Arrays.asList(
                                "http://localhost:*",
                                "http://127.0.0.1:*",
                                "https://localhost:*",
                                "https://127.0.0.1:*"));

                // 허용할 HTTP 메서드
                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

                // 허용할 헤더
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "X-Requested-With",
                                "Accept",
                                "Origin",
                                "Access-Control-Request-Method",
                                "Access-Control-Request-Headers"));

                // 노출할 헤더
                configuration.setExposedHeaders(Arrays.asList(
                                "Access-Control-Allow-Origin",
                                "Access-Control-Allow-Credentials",
                                "Authorization"));

                // 자격 증명 허용
                configuration.setAllowCredentials(true);

                // 프리플라이트 요청 캐시 시간 (초)
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/api/**", configuration);

                return source;
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