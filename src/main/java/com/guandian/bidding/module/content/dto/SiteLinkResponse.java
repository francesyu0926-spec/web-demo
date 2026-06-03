package com.guandian.bidding.module.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "常用网站链接")
public class SiteLinkResponse {

    private Long id;
    private String name;
    private String url;
}
