package com.guandian.bidding.module.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role_application")
public class RoleApplication {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** MANAGER / EXPERT */
    private String applyRole;
    private String major;
    private Long attachId;
    /** 0待审 1通过 2驳回 */
    private Integer auditStatus;
    private String auditRemark;
    private Long auditBy;
    private LocalDateTime auditTime;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
