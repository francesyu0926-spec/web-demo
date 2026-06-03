package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "报价确认情况")
public class PriceConfirmResponse {

    private Long projectId;
    private List<PriceItem> items;

    @Data
    @Builder
    public static class PriceItem {

        private Long registrationId;
        private String companyName;
        private BigDecimal bidPrice;
        private Integer decryptStatus;
        private Boolean ready;
    }
}
