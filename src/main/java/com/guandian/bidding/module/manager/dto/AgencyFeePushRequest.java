package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(description = "推送代理费")
public class AgencyFeePushRequest {

    @NotNull
    @Schema(description = "1固定金额 2标准折扣")
    private Integer feeMode;

    private BigDecimal amount;

    @Schema(description = "标准折扣(百分比)，feeMode=2 时使用")
    private BigDecimal discount;
}
