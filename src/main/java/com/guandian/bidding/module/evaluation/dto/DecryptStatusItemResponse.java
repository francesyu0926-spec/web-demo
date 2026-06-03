package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "投标文件解密情况")
public class DecryptStatusItemResponse {

    private Long registrationId;
    private String companyName;
    private Integer encrypted;
    private Integer decryptStatus;
    private LocalDateTime decryptTime;
}
