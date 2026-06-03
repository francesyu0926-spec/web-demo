package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_report")
public class EvaluationReport {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Integer totalDocs;
    private Integer generatedDocs;
    /** 0未出 1已完成 */
    private Integer status;
    private String purchaseContent;
    private String rejectNote;
    private String clarifyNote;
    private String candidateList;
    private Long exportAttachId;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
