package com.guandian.bidding.module.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "文件上传结果")
public class FileUploadResponse {

    private Long attachId;
    private String fileName;
    private String url;
    private Long fileSize;
    private String contentType;
    private String bizType;
}
