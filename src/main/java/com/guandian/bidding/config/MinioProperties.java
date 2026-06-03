package com.guandian.bidding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 对象存储配置（app.minio.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.minio")
public class MinioProperties {

    private boolean enabled = false;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
