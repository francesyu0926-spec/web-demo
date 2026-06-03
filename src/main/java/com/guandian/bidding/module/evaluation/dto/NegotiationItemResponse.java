package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "谈判磋商记录")
public class NegotiationItemResponse {

    private Long id;
    private Long projectId;
    private Long registrationId;
    private Long initiatorId;
    private String content;
    private Long attachId;
    private Integer status;
    private String replyContent;
    private Long replyAttachId;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
}
