package com.guandian.bidding.controller;

import com.guandian.bidding.common.api.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查，用于验证服务启动与文档可访问。
 */
@Tag(name = "系统", description = "健康检查")
@RestController
@RequestMapping("/api/ping")
public class PingController {

    @Operation(summary = "健康检查")
    @GetMapping
    public R<Map<String, Object>> ping() {
        return R.ok(Map.of(
                "app", "bidding-platform",
                "status", "UP",
                "time", LocalDateTime.now().toString()));
    }
}
