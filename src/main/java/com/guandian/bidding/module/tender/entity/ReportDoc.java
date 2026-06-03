package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report_doc")
public class ReportDoc {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reportId;
    private String docName;
    /** NOT_GEN / PENDING_SIGN / DONE */
    private String status;
    private Long attachId;
    private Long signedBy;
    private LocalDateTime signTime;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
