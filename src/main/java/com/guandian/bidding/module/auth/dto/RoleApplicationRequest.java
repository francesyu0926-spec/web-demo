package com.guandian.bidding.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "角色申请")
public class RoleApplicationRequest {

    @NotBlank(message = "申请角色不能为空")
    @Schema(description = "MANAGER / EXPERT", example = "MANAGER")
    private String applyRole;

    @Schema(description = "专家专业（申请 EXPERT 时建议填写）")
    private String major;

    @Schema(description = "证明材料附件ID")
    private Long attachId;
}
