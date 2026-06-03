package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "评审得分汇总")
public class ScoreSummaryResponse {

    private Long projectId;
    private List<BidderScore> bidders;

    @Data
    @Builder
    public static class BidderScore {

        private Long registrationId;
        private String companyName;
        private BigDecimal commerceScore;
        private BigDecimal techScore;
        private BigDecimal priceScore;
        private BigDecimal totalScore;
        private Integer rank;
        private List<ExpertScoreDetail> expertDetails;
    }

    @Data
    @Builder
    public static class ExpertScoreDetail {

        private Long expertId;
        private BigDecimal commerceScore;
        private BigDecimal techScore;
        private BigDecimal priceScore;
        private BigDecimal totalScore;
        private Boolean submitted;
    }
}
