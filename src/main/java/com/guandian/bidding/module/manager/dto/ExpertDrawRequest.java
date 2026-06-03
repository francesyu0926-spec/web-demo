package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "抽取/邀请专家")
public class ExpertDrawRequest {

    @Schema(description = "AM/PM/ALL")
    private String evalPeriod;

    private String reportPlace;

    @Schema(description = "1邀请 2随机抽取")
    private Integer drawType;

    @Schema(description = "专业领域(随机抽取时筛选)")
    private List<String> majors;

    @Schema(description = "随机抽取人数")
    private Integer count;

    @Schema(description = "邀请的专家userId列表")
    private List<Long> expertIds;
}
