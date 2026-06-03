package com.guandian.bidding.module.tender.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("expert_profile")
public class ExpertProfile {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String expertNo;
    private String major;
    private String org;
    private String title;
    private String idCard;
    private Long signImgId;
    /** 0停用 1正常 */
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
