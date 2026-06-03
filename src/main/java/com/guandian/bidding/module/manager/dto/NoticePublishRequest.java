package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发布中标通知书")
public class NoticePublishRequest {

    private Long attachId;

    private String title;

    private String content;
}
