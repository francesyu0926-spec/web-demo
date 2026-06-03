package com.guandian.bidding.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "角色切换请求")
public class SwitchRoleRequest {

    @NotBlank(message = "角色不能为空")
    @Schema(description = "目标角色码，如 BIDDER / MANAGER / EXPERT", example = "BIDDER")
    private String role;
}
