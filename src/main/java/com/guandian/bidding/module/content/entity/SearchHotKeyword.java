package com.guandian.bidding.module.content.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("search_hot_keyword")
public class SearchHotKeyword {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private Long searchCount;
}
