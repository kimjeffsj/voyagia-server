package com.voyagia.backend.config.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 🔴 RedisProperties 클래스
 * <p>
 * 역할: Redis 캐시 서버 연결 설정을 관리하는 클래스
 * <p>
 * Redis란?
 * - 인메모리 데이터베이스 (RAM에 데이터 저장)
 * - 매우 빠른 속도 (캐시용으로 주로 사용)
 * - 세션, 임시 데이터 저장 등에 활용
 */
@ConfigurationProperties(prefix = "voyagia.redis")
@Component
public class RedisProperties {

    // Redis connection information
    private String host;
    private int port;
    private String password;
    private int database;
    private long timeout;

    // Connection pool settings
    private int maxActive;
    private long maxWait;

    // Default Settings
    public RedisProperties() {
        this.host = "localhost";
        this.port = 6379;
        this.password = null;
        this.database = 0;
        this.timeout = 60000;
        this.maxActive = 8;
        this.maxWait = -1;
    }

    // Getter/Setter Methods
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    // Utility method: redis URL 생성
    public String getRedisUrl() {
        if (password != null && !password.isEmpty()) {
            return String.format("redis://:%s@%s:%d/%d", password, host, port, database);
        } else {
            return String.format("redis://%s:%d/%d", host, port, database);
        }
    }

    // Debugging method
    @Override
    public String toString() {
        return String.format("RedisProperties{host='%s', port=%d, database=%d}", host, port, database);
    }
}
