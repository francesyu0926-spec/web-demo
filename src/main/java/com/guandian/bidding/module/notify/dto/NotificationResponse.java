package com.guandian.bidding.module.notify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "消息通知")
public class NotificationResponse {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "类型: AUDIT/INVITE/OPEN/NEGOTIATION/REPORT/AWARD/SYSTEM")
    private String type;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "关联业务ID(报名/项目/邀请等)")
    private Long bizId;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
