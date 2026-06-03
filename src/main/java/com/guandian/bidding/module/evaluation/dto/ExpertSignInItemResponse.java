package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "专家签到情况")
public class ExpertSignInItemResponse {

    private Long assignmentId;
    private Long expertId;
    private String expertNo;
    private String major;
    private String status;
    private LocalDateTime signTime;
}
