package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "流程进度节点")
public class ProgressStepDto {

    private String step;
    private String status;
    private LocalDateTime time;
}
