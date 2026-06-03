package com.guandian.bidding.module.tender.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "招标公告列表项")
public class AnnouncementItemResponse {

    private Long id;
    private Long projectId;
    private String projectNo;
    private String projectName;
    private String region;
    private String tenderType;
    private String procurementType;
    private BigDecimal budget;
    private LocalDateTime publishTime;
    private LocalDateTime bidOpenTime;
    private LocalDateTime regEnd;
}
