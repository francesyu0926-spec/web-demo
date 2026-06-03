package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("payment_order_item")
public class PaymentOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    /** FILE / PLATFORM / AGENCY */
    private String feeType;
    private BigDecimal amount;
}
