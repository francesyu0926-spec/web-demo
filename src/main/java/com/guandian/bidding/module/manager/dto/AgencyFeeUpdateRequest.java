package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(description = "修改代理费")
public class AgencyFeeUpdateRequest {

    @NotNull
    private Long registrationId;

    @NotNull
    private BigDecimal amount;
}
