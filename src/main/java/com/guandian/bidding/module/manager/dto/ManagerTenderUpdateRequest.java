package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "更新招标项目(发布变更)")
public class ManagerTenderUpdateRequest {

    private String name;
    private String section;
    private String industry;
    private String region;
    private BigDecimal budget;
    private BigDecimal fileFee;
    private BigDecimal platformFee;
    private LocalDateTime regStart;
    private LocalDateTime regEnd;
    private LocalDateTime bidOpenTime;
    private String content;
    private Long bidFileId;
}
