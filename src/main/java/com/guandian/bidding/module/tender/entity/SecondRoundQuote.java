package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("second_round_quote")
public class SecondRoundQuote {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long registrationId;
    private Long initiatorId;
    private String content;
    /** 0未回复 1已回复 */
    private Integer status;
    private BigDecimal replyPrice;
    private String replyDuration;
    private Long replyAttachId;
    private LocalDateTime replyTime;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
