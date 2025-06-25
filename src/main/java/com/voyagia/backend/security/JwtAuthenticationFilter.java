package com.voyagia.backend.security;

import com.voyagia.backend.entity.User;
import com.voyagia.backend.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * JWT Authentication Filter
 * <p>
 * 모든 HTTP 요청에 대해 JWT 토큰을 검증하고 SecurityContext에 인증 정보를 설정하는 필터
 * Spring Security의 OncePerRequestFilter를 상속받아 요청당 한 번만 실행됨
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Lazy
    private UserService userService;

    /**
     * Validate JWT token and authorization
     *
     * @param request     HTTP request
     * @param response    HTTP response
     * @param filterChain filter chain
     * @throws ServletException servlet exception
     * @throws IOException      io exception
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. 요청에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);

            // 2. 토큰이 있고 현재 SecurityContext에 인증 정보가 없는 경우
            if (StringUtils.hasText(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 3. 토큰에서 사용자 정보 추출
                String username = jwtUtil.getUsernameFromToken(jwt);
                Long userId = jwtUtil.getUserIdFromToken(jwt);

                if (StringUtils.hasText(username) && userId != null) {

                    // 4. 사용자 존재 여부 확인
                    Optional<User> userOptional = userService.findByIdOptional(userId);

                    if (userOptional.isPresent()) {
                        User user = userOptional.get();

                        // 5. 토큰 유효성 검증
                        if (jwtUtil.validateToken(jwt, user)) {

                            // 6. 사용자가 활성화된 상태인지 확인
                            if (user.getIsActive()) {

                                // 7. 권한 설정
                                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                                // 8. 인증 토큰 생성
                                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        authorities);

                                // 9. 요청 세부 정보 설정
                                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                // 10. SecurityContext에 인증 정보 설정
                                SecurityContextHolder.getContext().setAuthentication(authToken);

                                logger.debug("JWT authentication successful for user: {} (ID: {})",
                                        username, userId);

                            } else {
                                logger.warn("Authentication failed - user is deactivated: {} (ID: {})",
                                        username, userId);
                            }

                        } else {
                            logger.warn("JWT token validation failed for user: {} (ID: {})",
                                    username, userId);
                        }

                    } else {
                        logger.warn("User not found for JWT token: {} (ID: {})", username, userId);
                    }

                } else {
                    logger.warn("Invalid JWT token - missing username or userId");
                }

            }

        } catch (Exception e) {
            logger.error("JWT authentication error: {}", e.getMessage());
            // 인증 실패 시 SecurityContext 클리어
            SecurityContextHolder.clearContext();
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from HTTP request
     *
     * @param request HTTP request
     * @return JWT token (Without Bearer prefix)
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * 특정 요청에 대해 필터를 건너뛸지 결정
     *
     * @param request HTTP 요청
     * @return true면 필터 건너뛰기, false면 필터 적용
     * @throws ServletException 서블릿 예외
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 인증이 필요 없는 경로들
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/users/register") ||
                path.startsWith("/api/users/check-") ||
                path.startsWith("/health") ||
                path.startsWith("/actuator") ||
                path.startsWith("/test") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars");
    }
}
