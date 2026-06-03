package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "商务/技术/报价评分提交")
public class ExpertScoreSubmitRequest {

    @NotNull(message = "submitted 不能为空")
    @Schema(description = "false暂存 true正式提交")
    private Boolean submitted;

    @NotEmpty(message = "scores 不能为空")
    @Valid
    private List<ScoreItem> scores;

    @Data
    @Schema(description = "单项评分")
    public static class ScoreItem {

        @NotNull
        private Long registrationId;

        @NotNull
        private Long itemId;

        @NotNull
        private BigDecimal score;
    }
}
