package com.guandian.bidding.module.manager.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentItemDto {

    private String feeType;
    private BigDecimal amount;
}
