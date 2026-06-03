package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "发起二轮报价")
public class SecondQuoteCreateRequest {

    @NotNull
    private Long registrationId;

    private String content;
}
