package com.guandian.bidding.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端。仅当 app.minio.enabled=true 时装配，避免未部署 MinIO 时影响启动。
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.minio", name = "enabled", havingValue = "true")
public class MinioConfig {

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
