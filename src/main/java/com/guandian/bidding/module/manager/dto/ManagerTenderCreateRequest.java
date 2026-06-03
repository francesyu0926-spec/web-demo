package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "创建/发布招标项目")
public class ManagerTenderCreateRequest {

    @NotBlank(message = "项目名称不能为空")
    private String name;

    private String section;

    @NotBlank(message = "采购方式不能为空")
    @Schema(description = "PUBLIC/INVITE/INQUIRY/SINGLE/NEGOTIATION/CONSULTATION")
    private String procurementType;

    @Schema(description = "ENGINEER/GOODS/SERVICE")
    private String tenderType;

    private String industry;
    private String region;
    private BigDecimal budget;
    private Long tendererId;
    private Long agencyId;
    private BigDecimal fileFee;
    private BigDecimal platformFee;

    @NotNull(message = "报名开始时间不能为空")
    private LocalDateTime regStart;

    @NotNull(message = "报名截止时间不能为空")
    private LocalDateTime regEnd;

    @NotNull(message = "开标时间不能为空")
    private LocalDateTime bidOpenTime;

    private String content;
    private Long bidFileId;

    @Schema(description = "true=直接发布为投标中(BIDDING)，false=保存草稿(DRAFT)")
    private Boolean publish;
}
