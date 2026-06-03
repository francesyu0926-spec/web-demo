package com.guandian.bidding.module.file.storage;

import com.guandian.bidding.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.minio", name = "enabled", havingValue = "true")
public class MinioFileStorageService implements FileStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @PostConstruct
    public void init() throws Exception {
        String bucket = minioProperties.getBucket();
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("已创建 MinIO bucket: {}", bucket);
        }
    }

    @Override
    public StoredFile store(String originalFilename, String contentType, InputStream inputStream, long size)
            throws IOException {
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String safeName = originalFilename != null ? originalFilename.replaceAll("[\\\\/:*?\"<>|]", "_") : "file";
            String fileKey = dateDir + "/" + UUID.randomUUID() + "_" + safeName;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(fileKey)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            return new StoredFile(fileKey, size);
        } catch (Exception e) {
            throw new IOException("MinIO 上传失败", e);
        }
    }

    @Override
    public Resource loadAsResource(String fileKey) throws IOException {
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(fileKey)
                    .build());
            return new InputStreamResource(stream);
        } catch (Exception e) {
            throw new IOException("MinIO 下载失败", e);
        }
    }
}
