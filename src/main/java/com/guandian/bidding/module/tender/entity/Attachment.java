package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("attachment")
public class Attachment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String fileKey;
    private Long fileSize;
    private String contentType;
    /** bid_doc/apply/license/article/report/general */
    private String bizType;
    private Long createBy;
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
