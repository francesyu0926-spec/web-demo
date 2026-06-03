package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tender_project")
public class TenderProject {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String projectNo;
    private String name;
    private String section;
    private String procurementType;
    private String tenderType;
    private String industry;
    private String region;
    private BigDecimal budget;
    private Long managerId;
    private Long tendererId;
    private Long agencyId;
    private BigDecimal fileFee;
    private BigDecimal platformFee;
    private String status;
    private String evalNode;
    private LocalDateTime regStart;
    private LocalDateTime regEnd;
    private LocalDateTime bidOpenTime;
    private BigDecimal evalTotalScore;
    private Integer priceScoreMethod;
    private Integer archived;
    private String content;
    private Long bidFileId;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
