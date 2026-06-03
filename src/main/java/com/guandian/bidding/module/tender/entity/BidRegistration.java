package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bid_registration")
public class BidRegistration {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long supplierId;
    private String companyName;
    private String contactName;
    private String contactPhone;
    private Long applyFileId;
    private Integer auditStatus;
    private String auditRemark;
    private String regStatus;
    private String bidStatus;
    private LocalDateTime regTime;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
