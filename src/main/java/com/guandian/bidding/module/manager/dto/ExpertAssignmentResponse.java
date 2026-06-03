package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "评标专家分配")
public class ExpertAssignmentResponse {

    private Long id;
    private Long expertId;
    private String expertNo;
    private String major;
    private String org;
    private String title;
    private Integer isLeader;
    private String evalPeriod;
    private String reportPlace;
    private Integer drawType;
    private String status;
    private LocalDateTime signTime;
}
