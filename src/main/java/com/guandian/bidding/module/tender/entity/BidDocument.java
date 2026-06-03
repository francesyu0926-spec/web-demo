package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("bid_document")
public class BidDocument {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long registrationId;
    private Long attachId;
    private BigDecimal bidPrice;
    private String duration;
    private String encryptPwd;
    private Integer encrypted;
    private Integer decryptStatus;
    private LocalDateTime decryptTime;
    private Long signImgId;
    private LocalDateTime submitTime;
    private LocalDateTime withdrawTime;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
