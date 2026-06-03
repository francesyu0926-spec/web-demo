package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "开标表格")
public class OpenTableResponse {

    private Long projectId;
    private List<OpenTableRow> rows;

    @Data
    @Builder
    public static class OpenTableRow {

        private Long registrationId;
        private String companyName;
        private BigDecimal bidPrice;
        private String duration;
        private Integer decryptStatus;
        private LocalDateTime decryptTime;
    }
}
