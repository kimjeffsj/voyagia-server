package com.voyagia.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 🔧 ApplicationProperties 클래스
 * <p>
 * 역할: 애플리케이션 기본 설정을 관리하는 클래스
 *
 * @ConfigurationProperties("voyagia.app") → application.yml에서 voyagia.app.xxx 형태의 설정들을 자동으로 이 클래스에 매핑
 * @Component → Spring이 이 클래스를 자동으로 Bean으로 등록 (의존성 주입 가능)
 */
@ConfigurationProperties(prefix = "voyagia.app")
@Component
public class ApplicationProperties {

    // 📝 필드 설명:
    // private String name; → application.yml의 voyagia.app.name 값이 자동으로 들어옴
    // 기본값은 application.yml에서 ${APP_NAME:voyagia-backend} 형태로 설정

    private String name;
    private String version;
    private String description;

    // Security 관련 설정을 중첩 클래스로 관리
    private Security security = new Security();

    // JWT 관련 설정을 중첩 클래스로 관리  
    private Jwt jwt = new Jwt();

    // 🔄 Getter/Setter 메서드들
    // Spring이 application.yml의 값을 이 메서드들을 통해 주입합니다

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    // 📦 중첩 클래스: Security 설정
    // voyagia.app.security.xxx 형태의 설정들을 관리
    public static class Security {
        private String username;
        private String password;
        private String[] roles;

        // Getter/Setter 메서드들
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String[] getRoles() {
            return roles;
        }

        public void setRoles(String[] roles) {
            this.roles = roles;
        }
    }

    // 📦 중첩 클래스: JWT 설정
    // voyagia.app.jwt.xxx 형태의 설정들을 관리
    public static class Jwt {
        private String secret;
        private long expirationMs;

        // Getter/Setter 메서드들
        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpirationMs() {
            return expirationMs;
        }

        public void setExpirationMs(long expirationMs) {
            this.expirationMs = expirationMs;
        }
    }
}