package com.guandian.bidding.module.file.storage;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorageService {

    StoredFile store(String originalFilename, String contentType, InputStream inputStream, long size)
            throws IOException;

    Resource loadAsResource(String fileKey) throws IOException;

    class StoredFile {
        private final String fileKey;
        private final long size;

        public StoredFile(String fileKey, long size) {
            this.fileKey = fileKey;
            this.size = size;
        }

        public String getFileKey() {
            return fileKey;
        }

        public long getSize() {
            return size;
        }
    }
}
