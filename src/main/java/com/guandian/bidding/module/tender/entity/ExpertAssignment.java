package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("expert_assignment")
public class ExpertAssignment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long expertId;
    private Integer isLeader;
    private String evalPeriod;
    private String reportPlace;
    /** 1邀请 2随机抽取 */
    private Integer drawType;
    /** PENDING/ACCEPTED/REJECTED/SIGNED */
    private String status;
    private LocalDateTime respondTime;
    private LocalDateTime signTime;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
