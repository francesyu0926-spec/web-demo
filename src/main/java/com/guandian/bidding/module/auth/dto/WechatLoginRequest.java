package com.guandian.bidding.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "微信登录")
public class WechatLoginRequest {

    @NotBlank(message = "code 不能为空")
    @Schema(description = "微信 OAuth code")
    private String code;
}
