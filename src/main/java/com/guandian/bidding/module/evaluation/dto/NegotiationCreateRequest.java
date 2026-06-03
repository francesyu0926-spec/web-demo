package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "发起谈判磋商")
public class NegotiationCreateRequest {

    @NotNull
    private Long registrationId;

    private String content;

    private Long attachId;
}
