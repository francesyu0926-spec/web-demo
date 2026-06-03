package com.guandian.bidding.module.bidder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "缴费订单")
public class PaymentOrderResponse {

    private String orderNo;
    private BigDecimal totalAmount;
    private Integer status;
    private String payChannel;
    private List<PaymentOrderItemResponse> items;
}
