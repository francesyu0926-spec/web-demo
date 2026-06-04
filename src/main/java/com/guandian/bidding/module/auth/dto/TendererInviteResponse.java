package com.guandian.bidding.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "招标人邀请")
public class TendererInviteResponse {

    private Long id;
    private Long inviterId;
    private String inviterName;
    private Long inviteeUserId;
    private String inviteeName;
    private String inviteePhone;
    @Schema(description = "0待接收 1已接收 2已拒绝")
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
