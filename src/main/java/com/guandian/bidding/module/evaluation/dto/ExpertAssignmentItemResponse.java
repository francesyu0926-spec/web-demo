package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "专家任务列表项")
public class ExpertAssignmentItemResponse {

    private Long assignmentId;
    private Long projectId;
    private String projectNo;
    private String projectName;
    private String projectStatus;
    private LocalDateTime bidOpenTime;
    private Integer isLeader;
    private String assignmentStatus;
    private LocalDateTime signTime;
}
