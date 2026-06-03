package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "谈判磋商回复")
public class NegotiationReplyRequest {

    private String content;

    private Long attachId;
}
