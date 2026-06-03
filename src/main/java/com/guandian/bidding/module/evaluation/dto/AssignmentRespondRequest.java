package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "专家确认/拒绝邀请")
public class AssignmentRespondRequest {

    @NotNull(message = "accept 不能为空")
    @Schema(description = "true接受 false拒绝")
    private Boolean accept;
}
