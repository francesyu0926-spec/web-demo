package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "评标报告导出")
public class ReportExportResponse {

    private Long projectId;
    private Long exportAttachId;
    private String message;
}
