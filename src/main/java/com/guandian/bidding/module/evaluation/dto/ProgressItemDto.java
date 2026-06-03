package com.guandian.bidding.module.evaluation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgressItemDto {

    private String name;
    private String status;
    private String detail;
}
