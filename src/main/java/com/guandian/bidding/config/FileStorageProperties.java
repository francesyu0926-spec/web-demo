package com.guandian.bidding.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {

    /** 本地存储根目录（MinIO 未启用时使用） */
    private String localDir = "./data/uploads";

    /** 单文件最大 MB */
    private int maxSizeMb = 50;
}
