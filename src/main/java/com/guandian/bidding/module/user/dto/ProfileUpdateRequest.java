package com.guandian.bidding.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Schema(description = "更新个人资料")
public class ProfileUpdateRequest {

    @Size(max = 64, message = "姓名最长64位")
    private String realName;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 128, message = "邮箱最长128位")
    private String email;

    @Size(max = 255, message = "头像URL最长255位")
    private String avatar;
}
