package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "二轮报价记录")
public class SecondQuoteItemResponse {

    private Long id;
    private Long projectId;
    private Long registrationId;
    private Long initiatorId;
    private String content;
    private Integer status;
    private BigDecimal replyPrice;
    private String replyDuration;
    private Long replyAttachId;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
}
