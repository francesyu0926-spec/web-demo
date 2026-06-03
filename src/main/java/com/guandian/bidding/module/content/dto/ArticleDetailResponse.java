package com.guandian.bidding.module.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "文章详情")
public class ArticleDetailResponse {

    private Long id;
    private String categoryCode;
    private String categoryName;
    private String title;
    private String content;
    private Long attachId;
    private LocalDateTime publishTime;
}
