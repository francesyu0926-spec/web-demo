package com.guandian.bidding.module.tender.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "招标人-项目详情(只读)")
public class TendererProjectDetailResponse {

    private Long id;
    private String projectNo;
    private String name;
    private String section;
    private String procurementType;
    private String tenderType;
    private String industry;
    private String region;
    private BigDecimal budget;
    private String status;
    private String evalNode;
    private LocalDateTime regStart;
    private LocalDateTime regEnd;
    private LocalDateTime bidOpenTime;
    private String content;
    private String managerName;
    private Long registrationCount;
}
