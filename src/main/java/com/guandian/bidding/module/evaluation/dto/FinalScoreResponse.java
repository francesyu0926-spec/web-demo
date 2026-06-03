package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "最终得分及候选人")
public class FinalScoreResponse {

    private Long projectId;
    private List<CandidateItem> candidates;

    @Data
    @Builder
    public static class CandidateItem {

        private Long registrationId;
        private String companyName;
        private Boolean qualified;
        private BigDecimal commerceScore;
        private BigDecimal techScore;
        private BigDecimal priceScore;
        private BigDecimal totalScore;
        private Integer rank;
    }
}
