package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "报名列表项")
public class RegistrationSummaryResponse {

    private Long id;
    private Long projectId;
    private String projectName;
    private Long supplierId;
    private String companyName;
    private String contactName;
    private String contactPhone;
    private Integer auditStatus;
    private String regStatus;
    private String bidStatus;
    private LocalDateTime regTime;
}
