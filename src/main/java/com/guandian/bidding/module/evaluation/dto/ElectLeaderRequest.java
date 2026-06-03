package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "推选专家组长")
public class ElectLeaderRequest {

    @NotNull(message = "leaderExpertId 不能为空")
    @Schema(description = "被推选为组长的专家用户ID")
    private Long leaderExpertId;
}
