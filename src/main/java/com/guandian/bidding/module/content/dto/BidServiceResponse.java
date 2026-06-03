package com.guandian.bidding.module.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "投标服务入口检查")
public class BidServiceResponse {

    @Schema(description = "是否可直接进入投标服务")
    private boolean allowed;

    @Schema(description = "是否需要切换为投标人身份")
    private boolean needSwitchRole;

    private String message;
}
