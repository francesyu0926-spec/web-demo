package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "符合性审查汇总")
public class ComplianceResultResponse {

    private Long projectId;
    private List<BidderCompliance> bidders;

    @Data
    @Builder
    public static class BidderCompliance {

        private Long registrationId;
        private String companyName;
        private Boolean passed;
        private List<ItemResult> items;
    }

    @Data
    @Builder
    public static class ItemResult {

        private Long itemId;
        private String type;
        private String name;
        private Boolean passed;
        private Integer passCount;
        private Integer failCount;
        private List<ExpertVote> expertVotes;
    }

    @Data
    @Builder
    public static class ExpertVote {

        private Long expertId;
        private Integer pass;
    }
}
