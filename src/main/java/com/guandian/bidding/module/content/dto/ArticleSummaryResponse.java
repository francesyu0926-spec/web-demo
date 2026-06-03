package com.guandian.bidding.module.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "文章列表项")
public class ArticleSummaryResponse {

    private Long id;
    private String categoryCode;
    private String title;
    private LocalDateTime publishTime;
}
