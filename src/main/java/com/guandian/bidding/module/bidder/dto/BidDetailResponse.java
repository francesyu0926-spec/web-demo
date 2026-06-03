package com.guandian.bidding.module.bidder.dto;

import com.guandian.bidding.module.manager.dto.ProgressStepDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Schema(description = "投标详情")
public class BidDetailResponse {

    private Long registrationId;
    private Long projectId;
    private String projectName;
    private String projectStatus;
    private String evalNode;
    private String regStatus;
    private String bidStatus;
    private BidDocumentResponse bidDocument;
    private BigDecimal agencyFee;
    private Integer agencyFeePaid;
    private List<ProgressStepDto> progress;
}
