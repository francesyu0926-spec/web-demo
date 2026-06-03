package com.guandian.bidding.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "登录/注册响应")
public class AuthTokenResponse {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户拥有的角色列表")
    private List<String> roles;

    @Schema(description = "当前激活角色")
    private String activeRole;
}
