package com.guandian.bidding.module.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tenderer_invite")
public class TendererInvite {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long inviterId;
    private Long inviteeUserId;
    private String inviteeName;
    private String inviteePhone;
    /** 0待接收 1已接收 2已拒绝 */
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
