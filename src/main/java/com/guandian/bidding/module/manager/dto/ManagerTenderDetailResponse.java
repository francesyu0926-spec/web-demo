package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "项目经理-项目详情")
public class ManagerTenderDetailResponse {

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
    private BigDecimal fileFee;
    private BigDecimal platformFee;
    private BigDecimal evalTotalScore;
    private Integer priceScoreMethod;
    private String content;
    private Long bidFileId;
    private List<EvaluationItemDto> evalItems;
}
