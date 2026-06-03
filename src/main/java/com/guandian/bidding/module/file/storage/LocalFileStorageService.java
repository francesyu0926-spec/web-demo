package com.guandian.bidding.module.file.storage;

import com.guandian.bidding.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.minio", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final FileStorageProperties properties;
    private Path basePath;

    @PostConstruct
    public void init() throws IOException {
        basePath = Paths.get(properties.getLocalDir()).toAbsolutePath().normalize();
        Files.createDirectories(basePath);
        log.info("本地文件存储目录: {}", basePath);
    }

    @Override
    public StoredFile store(String originalFilename, String contentType, InputStream inputStream, long size)
            throws IOException {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String safeName = sanitizeFilename(originalFilename);
        String fileKey = dateDir + "/" + UUID.randomUUID() + "_" + safeName;
        Path target = basePath.resolve(fileKey).normalize();
        if (!target.startsWith(basePath)) {
            throw new IOException("非法文件路径");
        }
        Files.createDirectories(target.getParent());
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return new StoredFile(fileKey, Files.size(target));
    }

    @Override
    public Resource loadAsResource(String fileKey) throws IOException {
        Path file = basePath.resolve(fileKey).normalize();
        if (!file.startsWith(basePath) || !Files.exists(file)) {
            throw new IOException("文件不存在: " + fileKey);
        }
        return new FileSystemResource(file);
    }

    private String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
