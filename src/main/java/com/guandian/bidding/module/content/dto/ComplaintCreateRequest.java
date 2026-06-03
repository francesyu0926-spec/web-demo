package com.guandian.bidding.module.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "提交投诉建议")
public class ComplaintCreateRequest {

    private Long projectId;

    @NotBlank(message = "分类不能为空")
    @Schema(description = "ADVICE / COMPLAINT")
    private String category;

    @NotBlank(message = "子类型不能为空")
    private String subType;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最长200字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Long attachId;
}
