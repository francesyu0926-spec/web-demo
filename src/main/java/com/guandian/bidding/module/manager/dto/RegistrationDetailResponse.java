package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "报名详情")
public class RegistrationDetailResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private String companyName;
    private String contactName;
    private String contactPhone;
    private Long applyFileId;
    private Integer auditStatus;
    private String auditRemark;
    private String regStatus;
    private String bidStatus;
    private LocalDateTime regTime;
    private List<ProgressStepDto> progress;
}
