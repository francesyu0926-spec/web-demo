package com.guandian.bidding.module.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "发布中标公示")
public class AwardPublishRequest {

    @NotBlank
    private String title;

    private String content;

    private Long attachId;

    @NotEmpty
    @Valid
    private List<WinnerItem> winners;

    @Data
    public static class WinnerItem {

        @NotNull
        private Long registrationId;

        private BigDecimal finalPrice;
    }
}
