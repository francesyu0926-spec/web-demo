package com.guandian.bidding.module.bidder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "投标人-我的项目")
public class BidderProjectResponse {

    private Long projectId;
    private String projectNo;
    private String projectName;
    private String tenderType;
    private String projectStatus;
    private String evalNode;
    private Long registrationId;
    private String regStatus;
    private String bidStatus;
    private LocalDateTime bidOpenTime;
}
