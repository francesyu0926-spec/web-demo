package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "二轮报价回复")
public class SecondQuoteReplyRequest {

    private BigDecimal price;

    private String duration;

    private Long attachId;
}
