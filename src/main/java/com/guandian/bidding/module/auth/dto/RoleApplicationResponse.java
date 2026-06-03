package com.guandian.bidding.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "角色申请详情")
public class RoleApplicationResponse {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String applyRole;
    private String major;
    private Long attachId;
    /** 0待审 1通过 2驳回 */
    private Integer auditStatus;
    private String auditRemark;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
}
