package com.guandian.bidding.module.bidder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(description = "递交投标文件")
public class BidDocumentSubmitRequest {

    @NotNull(message = "附件ID不能为空")
    private Long attachId;

    private BigDecimal bidPrice;
    private String duration;
}
