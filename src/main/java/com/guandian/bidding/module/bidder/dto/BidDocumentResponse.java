package com.guandian.bidding.module.bidder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "投标文件信息")
public class BidDocumentResponse {

    private Long id;
    private Long attachId;
    private BigDecimal bidPrice;
    private String duration;
    private Integer encrypted;
    private Integer decryptStatus;
    private LocalDateTime submitTime;
    private LocalDateTime withdrawTime;
}
