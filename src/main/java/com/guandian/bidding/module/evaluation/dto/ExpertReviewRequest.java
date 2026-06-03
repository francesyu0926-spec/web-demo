package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "符合性审查提交")
public class ExpertReviewRequest {

    @NotEmpty(message = "scores 不能为空")
    @Valid
    private List<ReviewScoreItem> scores;

    @Data
    @Schema(description = "单项审查")
    public static class ReviewScoreItem {

        @NotNull
        private Long registrationId;

        @NotNull
        private Long itemId;

        @NotNull
        @Schema(description = "0不通过 1通过")
        private Integer pass;
    }
}
