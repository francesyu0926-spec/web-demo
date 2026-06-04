package com.guandian.bidding.module.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "操作审计日志")
public class OperationLogResponse {

    private Long id;
    private Long userId;
    private String roleCode;
    private String module;
    private String action;
    private Long bizId;
    private String detail;
    private String ip;
    private LocalDateTime createTime;
}
