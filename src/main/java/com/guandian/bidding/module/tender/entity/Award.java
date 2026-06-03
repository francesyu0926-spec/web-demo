package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("award")
public class Award {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long registrationId;
    @TableField("`rank`")
    private Integer rank;
    private BigDecimal finalScore;
    private BigDecimal finalPrice;
    private Integer isWinner;
    private LocalDateTime publicityStart;
    private LocalDateTime publicityEnd;
    private BigDecimal agencyFee;
    private Integer agencyFeePaid;
    private Long noticeAttachId;
    private LocalDateTime noticePublishTime;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
