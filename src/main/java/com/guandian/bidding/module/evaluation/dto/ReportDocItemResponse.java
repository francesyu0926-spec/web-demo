package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "评标报告文档项")
public class ReportDocItemResponse {

    private Long id;
    private String docName;
    private String status;
    private Long attachId;
    private Long signedBy;
    private LocalDateTime signTime;
}
