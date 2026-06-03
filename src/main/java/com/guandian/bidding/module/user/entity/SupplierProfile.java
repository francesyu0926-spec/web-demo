package com.guandian.bidding.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("supplier_profile")
public class SupplierProfile {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String companyName;
    private String creditCode;
    private String legalPerson;
    private String address;
    private String contactName;
    private String contactPhone;
    private String bankName;
    private String bankAccount;
    private Long licenseFileId;
    /** 0待认证 1已认证 2驳回 */
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
