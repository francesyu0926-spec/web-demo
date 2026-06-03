package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("negotiation")
public class Negotiation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long registrationId;
    private Long initiatorId;
    private String content;
    private Long attachId;
    /** 0未回复 1已回复 */
    private Integer status;
    private String replyContent;
    private Long replyAttachId;
    private LocalDateTime replyTime;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
