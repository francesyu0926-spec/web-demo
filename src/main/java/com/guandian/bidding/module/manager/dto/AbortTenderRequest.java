package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "废标")
public class AbortTenderRequest {

    @NotBlank(message = "废标原因不能为空")
    private String reason;
}
