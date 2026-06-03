package com.guandian.bidding.module.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "投诉处理反馈")
public class ComplaintFeedbackRequest {

    @NotBlank(message = "reply 不能为空")
    private String reply;
}
