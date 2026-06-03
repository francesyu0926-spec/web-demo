package com.guandian.bidding.module.content.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.content.dto.ArticleDetailResponse;
import com.guandian.bidding.module.content.dto.ArticleSummaryResponse;
import com.guandian.bidding.module.content.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "内容", description = "业务指南 / 政策法规 / 案例 / 下载 / 动态")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "内容列表")
    @GetMapping
    public R<PageResult<ArticleSummaryResponse>> list(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(articleService.list(categoryCode, pageNum, pageSize));
    }

    @Operation(summary = "内容详情")
    @GetMapping("/{id}")
    public R<ArticleDetailResponse> detail(@PathVariable Long id) {
        return R.ok(articleService.getDetail(id));
    }
}
