package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "编辑评审表")
public class EvalItemsUpdateRequest {

    @Schema(description = "评审总分")
    private BigDecimal totalScore;

    @Schema(description = "报价评分方式 1/2/3")
    private Integer priceScoreMethod;

    private List<EvaluationItemDto> items;
}
