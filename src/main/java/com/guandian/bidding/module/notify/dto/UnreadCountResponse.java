package com.guandian.bidding.module.notify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "未读消息数")
public class UnreadCountResponse {

    @Schema(description = "未读数量")
    private long count;
}
