package com.voyagia.backend.config;

import com.voyagia.backend.config.properties.S3Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    private final S3Properties s3Properties;

    public S3Config(S3Properties s3Properties) {
        this.s3Properties = s3Properties;
    }

    @Bean
    public S3Client s3Client() {
        // AWS 자격증명이 설정되지 않은 경우 기본 자격증명 제공자 체인 사용
        if (s3Properties.getAccessKey() == null || s3Properties.getAccessKey().isEmpty() ||
            s3Properties.getSecretKey() == null || s3Properties.getSecretKey().isEmpty()) {
            return S3Client.builder()
                    .region(Region.of(s3Properties.getRegion()))
                    .build();
        }

        // 명시적 자격증명 사용
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                s3Properties.getAccessKey(),
                s3Properties.getSecretKey()
        );

        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}