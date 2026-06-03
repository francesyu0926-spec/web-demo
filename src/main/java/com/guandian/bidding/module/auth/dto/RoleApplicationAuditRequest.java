package com.guandian.bidding.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "角色申请审核")
public class RoleApplicationAuditRequest {

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "1通过 2驳回", example = "1")
    private Integer auditStatus;

    @Schema(description = "审核备注")
    private String remark;
}
