package com.guandian.bidding.module.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "首页广告轮播")
public class BannerItemResponse {

    private Long id;
    private String title;
    private Long imageId;
    private String linkUrl;
}
