package com.guandian.bidding.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "我的偏好")
public class PreferenceResponse {

    private String industries;
    private String regions;
    private String types;
}
