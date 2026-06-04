package com.guandian.bidding.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "响应招标人邀请")
public class TendererInviteRespondRequest {

    @NotNull(message = "accept 不能为空")
    @Schema(description = "true=接受 false=拒绝")
    private Boolean accept;
}
