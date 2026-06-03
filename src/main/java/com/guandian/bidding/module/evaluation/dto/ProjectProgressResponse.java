package com.guandian.bidding.module.evaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "项目进度")
public class ProjectProgressResponse {

    private Long projectId;
    private String projectStatus;
    private String evalNode;
    private List<ProgressItemDto> items;
}
