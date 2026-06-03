package com.guandian.bidding.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Schema(description = "注册请求")
public class RegisterRequest {

    @Size(min = 4, max = 32, message = "用户名长度为4-32位")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    @Schema(description = "登录账号，不填则默认使用手机号")
    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度为6-32位")
    @Schema(description = "登录密码")
    private String password;

    @Schema(description = "短信验证码（填写手机号时必填）")
    private String smsCode;

    @Size(max = 64, message = "姓名最长64位")
    @Schema(description = "真实姓名")
    private String realName;
}
