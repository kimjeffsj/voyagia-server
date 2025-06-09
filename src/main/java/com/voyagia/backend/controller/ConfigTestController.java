package com.voyagia.backend.controller;

import com.voyagia.backend.config.properties.ApplicationProperties;
import com.voyagia.backend.config.properties.DatabaseProperties;
import com.voyagia.backend.config.properties.RedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 🧪 ConfigTestController 클래스
 * <p>
 * 역할: 환경변수와 Properties 클래스들이 제대로 작동하는지 테스트하는 컨트롤러
 *
 * @RestController → 이 클래스가 REST API 컨트롤러임을 Spring에게 알림
 * → 메서드의 리턴값이 자동으로 JSON으로 변환됨
 * @RequestMapping("/test") → 이 컨트롤러의 모든 URL이 /api/test로 시작 (application.yml에서 context-path: /api 설정)
 */
@RestController
@RequestMapping("/test")
public class ConfigTestController {

    // Dependency Injection (의존성 주입)
    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private DatabaseProperties databaseProperties;

    @Autowired
    private RedisProperties redisProperties;

    /**
     * API for all the settings information
     * <p>
     * URL: <a href="http://localhost:8080/api/test/config">...</a>
     */
    @GetMapping("/config")
    public Map<String, Object> getAllConfig() {
        Map<String, Object> config = new HashMap<>();

        // Application Settings
        Map<String, Object> appConfig = new HashMap<>();
        appConfig.put("name", applicationProperties.getName());
        appConfig.put("version", applicationProperties.getVersion());
        appConfig.put("description", applicationProperties.getDescription());
        appConfig.put("adminUsername", applicationProperties.getSecurity().getUsername());
        appConfig.put("jwtSecret", applicationProperties.getJwt().getSecret().substring(0, 10) + "...");  // 보안상 일부만 표시

        // Database Settings
        Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put("host", databaseProperties.getHost());
        dbConfig.put("port", databaseProperties.getPort());
        dbConfig.put("name", databaseProperties.getName());
        dbConfig.put("username", databaseProperties.getUsername());
        dbConfig.put("jdbcUrl", databaseProperties.getJdbcUrl());
        dbConfig.put("maxPoolSize", databaseProperties.getMaxPoolSize());

        // Redis Settings
        Map<String, Object> redisConfig = new HashMap<>();
        redisConfig.put("host", redisProperties.getHost());
        redisConfig.put("port", redisProperties.getPort());
        redisConfig.put("database", redisProperties.getDatabase());
        redisConfig.put("timeout", redisProperties.getTimeout());
        redisConfig.put("redisUrl", redisProperties.getRedisUrl());

        // 모든 설정을 하나의 Map에 담아서 리턴
        config.put("application", appConfig);
        config.put("database", dbConfig);
        config.put("redis", redisConfig);

        return config;
    }

    /**
     * Database settings only
     * <p>
     * URL: <a href="http://localhost:8080/api/test/database">...</a>
     */
    @GetMapping("/database")
    public DatabaseProperties getDatabaseConfig() {
        return databaseProperties;
    }

    /**
     * Redis settings only
     * <p>
     * URL: <a href="http://localhost:8080/adpi/test/redis">...</a>
     */
    @GetMapping("/redis")
    public RedisProperties getRedisConfig() {
        return redisProperties;
    }

    /**
     * Env test API
     * <p>
     * URL: <a href="http://localhost:8080/api/test/env-test">...</a>
     */
    @GetMapping("/env-test")
    public Map<String, String> testEnvironmentVariables() {
        Map<String, String> envTest = new HashMap<>();

        // 시스템 환경 변수 직접 읽기
        envTest.put("JAVA_HOME", System.getenv("JAVA_HOME"));
        envTest.put("DB_HOST", System.getenv("DB_HOST"));
        envTest.put("APP_NAME", System.getenv("APP_NAME"));

        // Properties 클래스를 통해 읽은 값과 비교
        envTest.put("properties_db_host", databaseProperties.getHost());
        envTest.put("properties_app_name", applicationProperties.getName());

        return envTest;
    }
}
