package com.guandian.bidding.module.bidder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "报名缴费")
public class PaymentRequest {

    @NotBlank(message = "支付渠道不能为空")
    @Schema(description = "WECHAT/ALIPAY/TRANSFER")
    private String payChannel;
}
