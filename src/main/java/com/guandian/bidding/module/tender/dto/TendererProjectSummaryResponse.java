package com.guandian.bidding.module.tender.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "招标人-我的项目列表项")
public class TendererProjectSummaryResponse {

    private Long id;
    private String projectNo;
    private String name;
    private String procurementType;
    private String tenderType;
    private String status;
    private String evalNode;
    private BigDecimal budget;
    private LocalDateTime bidOpenTime;
    private LocalDateTime regEnd;
    private String managerName;
    private LocalDateTime createTime;
}
