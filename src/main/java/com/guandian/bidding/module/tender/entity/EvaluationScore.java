package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("evaluation_score")
public class EvaluationScore {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long registrationId;
    private Long expertId;
    private Long itemId;
    /** 形式/资格/响应性: 0否 1是 */
    private Integer pass;
    /** 商务/技术/报价评分 */
    private BigDecimal score;
    private String remark;
    /** 0暂存 1已提交 */
    private Integer submitted;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
