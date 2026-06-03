package com.guandian.bidding.module.bidder.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentOrderItemResponse {

    private String feeType;
    private BigDecimal amount;
}
