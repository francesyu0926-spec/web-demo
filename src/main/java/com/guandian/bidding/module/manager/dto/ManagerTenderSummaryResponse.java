package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "项目经理-项目列表项")
public class ManagerTenderSummaryResponse {

    private Long id;
    private String projectNo;
    private String name;
    private String procurementType;
    private String tenderType;
    private String status;
    private String evalNode;
    private LocalDateTime bidOpenTime;
    private LocalDateTime regEnd;
    private Long registrationCount;
    private LocalDateTime createTime;
}
