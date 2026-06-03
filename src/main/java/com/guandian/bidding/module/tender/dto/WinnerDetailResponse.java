package com.guandian.bidding.module.tender.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "中标详情")
public class WinnerDetailResponse {

    private Long id;
    private Long projectId;
    private String projectNo;
    private String projectName;
    private String winnerCompany;
    private BigDecimal finalPrice;
    private BigDecimal finalScore;
    private LocalDateTime publicityStart;
    private LocalDateTime publicityEnd;
    private LocalDateTime noticePublishTime;
    private String content;
}
