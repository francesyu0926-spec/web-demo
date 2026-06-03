package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "解析招标文件结果")
public class ParseTenderResponse {

    private String name;
    private String procurementType;
    private String tenderType;
    private BigDecimal budget;
    private String content;
}
