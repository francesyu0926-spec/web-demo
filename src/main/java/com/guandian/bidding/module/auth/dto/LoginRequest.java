package com.guandian.bidding.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @NotBlank(message = "登录方式不能为空")
    @Schema(description = "PASSWORD / SMS / WECHAT", example = "PASSWORD")
    private String loginType;

    @NotBlank(message = "账号不能为空")
    @Schema(description = "用户名或手机号", example = "13800138000")
    private String account;

    @NotBlank(message = "凭证不能为空")
    @Schema(description = "密码或短信验证码")
    private String credential;
}
