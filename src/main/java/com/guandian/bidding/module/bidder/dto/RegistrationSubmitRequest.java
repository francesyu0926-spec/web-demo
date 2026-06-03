package com.guandian.bidding.module.bidder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "提交报名")
public class RegistrationSubmitRequest {

    private String companyName;
    private String contactName;
    private String contactPhone;

    @NotNull(message = "报名文件不能为空")
    private Long applyFileId;
}
