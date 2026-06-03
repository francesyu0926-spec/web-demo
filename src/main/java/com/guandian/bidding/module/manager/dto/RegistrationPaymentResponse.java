package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "缴费信息")
public class RegistrationPaymentResponse {

    private String orderNo;
    private BigDecimal totalAmount;
    private Integer status;
    private String payChannel;
    private LocalDateTime payTime;
    private List<PaymentItemDto> items;
}
