package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "专家可下载投标文件")
public class ExpertBidDocumentItemResponse {

    private Long registrationId;
    private String companyName;
    private Long bidDocumentId;
    private Long attachId;
    private BigDecimal bidPrice;
    private String duration;
    private Integer decryptStatus;
}
