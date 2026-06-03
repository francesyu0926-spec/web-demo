package com.guandian.bidding.module.file.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.file.dto.FileUploadResponse;
import com.guandian.bidding.module.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件", description = "上传 / 下载")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FileUploadResponse> upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) String bizType) {
        return R.ok(fileService.upload(file, bizType));
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        return fileService.download(id);
    }
}
