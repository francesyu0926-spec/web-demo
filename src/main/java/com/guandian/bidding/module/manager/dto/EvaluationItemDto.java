package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "评审项")
public class EvaluationItemDto {

    private Long id;
    @Schema(description = "FORMAL/QUALIFY/RESPONSE/COMMERCE/TECH/PRICE")
    private String type;
    private String name;
    private BigDecimal maxScore;
    private BigDecimal subTotal;
    private Integer sort;
}
