package com.guandian.bidding.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新我的偏好")
public class PreferenceUpdateRequest {

    @Schema(description = "关注行业，逗号分隔")
    private String industries;

    @Schema(description = "关注地区，逗号分隔")
    private String regions;

    @Schema(description = "关注招标类型，逗号分隔")
    private String types;
}
