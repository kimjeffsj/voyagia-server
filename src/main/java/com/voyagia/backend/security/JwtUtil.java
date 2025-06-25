package com.voyagia.backend.security;

import com.voyagia.backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT token utility class
 * <p>
 * JWT token create, validate, parse features
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // JWT Setting (injected in application.yml)
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInSeconds;

    /**
     * SecretKey
     * HMAC-SHA algorithms
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Create JWT token from user information
     *
     * @param user User
     * @return JWT token
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        claims.put("isActive", user.getIsActive());

        return createToken(claims, user.getUsername());
    }

    /**
     * Create JWT token
     *
     * @param claims  claims included in token
     * @param subject token(username)
     * @return JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInSeconds * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Extract username from token
     *
     * @param token JWT token
     * @return username
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extract user ID from token
     *
     * @param token JWT token
     * @return user ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extract email from token
     *
     * @param token JWT token
     * @return email
     */
    public String getEmailFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract role from
     *
     * @param token JWT token
     * @return user role
     */
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Extract Expiration Date from token
     *
     * @param token JWT token
     * @return Expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extract claim from token
     *
     * @param token          JWT token
     * @param claimsResolver claim extract function
     * @return extracted claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract claims from token
     *
     * @param token JWT token
     * @return All claims
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.warn("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.warn("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("JWT token compact of handler are invalid: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token
     * @return true/false
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Validate token
     *
     * @param token    JWT token
     * @param username username
     * @return true/false
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (username.equals(tokenUsername) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token ( User entity )
     *
     * @param token JWT Token
     * @param user  User entity
     * @return true/false
     */
    public Boolean validateToken(String token, User user) {
        try {
            final String username = getUsernameFromToken(token);
            final Long userId = getUserIdFromToken(token);

            return (user.getUsername().equals(username) &&
                    user.getId().equals(userId) &&
                    !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("JWT token validation failed for user {}: {}", user.getUsername(), e.getMessage());
            return false;
        }
    }

    /**
     * Remove Bearer prefix from token
     *
     * @param bearerToken "Bearer " included token
     * @return JWT token
     */
    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }

    /**
     * JWT expiration in seconds
     *
     * @return expiration in seconds
     */
    public long getExpirationInSeconds() {
        return jwtExpirationInSeconds;
    }

    /**
     * Log token information(debugging)
     *
     * @param token JWT token
     */
    public void logTokenInfo(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            logger.debug("Token Info - Subject: {}, Issued: {}, Expires: {}",
                    claims.getSubject(),
                    claims.getIssuedAt(),
                    claims.getExpiration());
        } catch (Exception e) {
            logger.warn("Failed to log token info: {}", e.getMessage());
        }
    }

    /**
     * Check if token is expiring soon
     *
     * @param token               JWT token
     * @param minutesBeforeExpiry minutes before expiry
     * @return true/false
     */
    public Boolean isTokenExpiringSoon(String token, int minutesBeforeExpiry) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            final Date now = new Date();
            final long timeDifference = expiration.getTime() - now.getTime();
            final long minutesDifference = timeDifference / (60 * 1000);

            return minutesDifference <= minutesBeforeExpiry;
        } catch (Exception e) {
            logger.warn("Failed to check token expiry: {}", e.getMessage());
            return true; // 안전을 위해 true 반환
        }
    }
}
