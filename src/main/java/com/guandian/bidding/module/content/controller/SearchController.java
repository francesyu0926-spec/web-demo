package com.guandian.bidding.module.content.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.content.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "搜索", description = "招标/中标搜索、热门词、历史")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "搜索招标或中标")
    @GetMapping
    public R<PageResult<?>> search(
            @RequestParam(defaultValue = "tender") String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String time,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(searchService.search(type, keyword, region, category, time, pageNum, pageSize));
    }

    @Operation(summary = "热门搜索 Top10")
    @GetMapping("/hot")
    public R<List<String>> hot() {
        return R.ok(searchService.hotKeywords());
    }

    @Operation(summary = "我的历史搜索（最近3条）")
    @GetMapping("/history")
    public R<List<String>> history() {
        return R.ok(searchService.searchHistory());
    }
}
