package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("evaluation_item")
public class EvaluationItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    /** FORMAL/QUALIFY/RESPONSE/COMMERCE/TECH/PRICE */
    private String type;
    private String name;
    private BigDecimal maxScore;
    private BigDecimal subTotal;
    private Integer sort;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
