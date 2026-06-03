package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编辑评标报告")
public class ReportUpdateRequest {

    private String purchaseContent;
    private String rejectNote;
    private String candidateList;
    private String clarifyNote;
}
