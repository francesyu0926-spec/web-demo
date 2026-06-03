package com.guandian.bidding.module.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "投诉建议详情")
public class ComplaintResponse {

    private Long id;
    private Long projectId;
    private String category;
    private String subType;
    private String title;
    private String content;
    private Long attachId;
    /** 0待处理 1处理中 2已回复 */
    private Integer status;
    private String reply;
    private Long handler;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
}
