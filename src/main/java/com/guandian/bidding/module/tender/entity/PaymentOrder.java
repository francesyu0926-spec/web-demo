package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_order")
public class PaymentOrder {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long projectId;
    private Long registrationId;
    private Long payerId;
    /** REGISTER / AGENCY */
    private String bizType;
    private BigDecimal totalAmount;
    private String payChannel;
    /** 0未支付 1已支付 2已退款 */
    private Integer status;
    private LocalDateTime payTime;
    private Integer feeMode;
    private BigDecimal discount;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
