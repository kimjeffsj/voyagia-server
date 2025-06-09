package com.voyagia.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 🗄️ DatabaseProperties 클래스
 * <p>
 * 역할: 데이터베이스 연결 설정을 관리하는 클래스
 *
 * @ConfigurationProperties("voyagia.database") → application.yml에서 voyagia.database.xxx 형태의 설정들을 이 클래스에 매핑
 * <p>
 * 장점:
 * 1. IDE에서 자동완성 지원
 * 2. 오타 방지 (컴파일 시 체크)
 * 3. 타입 안전성 (String, int 등)
 */
@ConfigurationProperties(prefix = "voyagia.database")
@Component
public class DatabaseProperties {

    // Database 연결 정보
    private String host;        // DB host
    private int port;           // DB port
    private String name;        // DB name
    private String username;    // DB userName
    private String password;    // DB password

    // Connection pool(optimization)
    private int maxPoolSize;    // Maximum connection size
    private int minPoolSize;    // Minimum connection size
    private long connectionTimeout; // Connection timeout(ms)

    // Default Settings
    public DatabaseProperties() {
        this.host = "localhost";
        this.port = 5432;
        this.name = "voyagia";
        this.username = "postgres";
        this.password = "postgres";
        this.maxPoolSize = 20;
        this.minPoolSize = 5;
        this.connectionTimeout = 30000;
    }

    // Getter/Setter Methods
    // Spring이 환경 변수나 application.yml 값을 주입할 때 사용
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    // 유틸리티 메서드: JDBC URL 자동 생성
    // 다른 클래스에서 databaseProperties.getJdbcUrl() 형태로 사용 가능
    public String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%d/%s", host, port, name);
    }

    // Debugging method: print current information
    @Override
    public String toString() {
        return String.format("DatabaseProperties{host='%s', port=%d, name='%s', username='%s'", host, port, name, username);
    }
}
