package com.guandian.bidding.module.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("article_category")
public class ArticleCategory {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
}
