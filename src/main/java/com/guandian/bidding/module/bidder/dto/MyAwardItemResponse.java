package com.guandian.bidding.module.bidder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "我的中标项目")
public class MyAwardItemResponse {

    private Long projectId;
    private String projectNo;
    private String projectName;
    private Long registrationId;
    private Integer rank;
    private BigDecimal finalPrice;
    private LocalDateTime publicityStart;
    private LocalDateTime noticePublishTime;
}
