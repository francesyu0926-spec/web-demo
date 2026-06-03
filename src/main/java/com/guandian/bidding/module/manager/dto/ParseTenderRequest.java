package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "解析招标文件(占位)")
public class ParseTenderRequest {

    @NotNull(message = "附件ID不能为空")
    private Long attachId;
}
