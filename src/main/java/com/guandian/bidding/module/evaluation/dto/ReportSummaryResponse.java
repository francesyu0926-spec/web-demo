package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "评标报告汇总")
public class ReportSummaryResponse {

    private Long projectId;
    private Long reportId;
    private Integer totalDocs;
    private Integer generatedDocs;
    private Integer status;
    private String purchaseContent;
    private String rejectNote;
    private String clarifyNote;
    private String candidateList;
    private Long exportAttachId;
    private List<ReportDocItemResponse> docs;
}
