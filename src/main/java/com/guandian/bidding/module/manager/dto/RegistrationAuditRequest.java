package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "报名审核")
public class RegistrationAuditRequest {

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "1通过 2驳回")
    private Integer auditStatus;

    private String remark;
}
