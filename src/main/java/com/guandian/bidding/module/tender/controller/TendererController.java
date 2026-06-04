package com.guandian.bidding.module.tender.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.tender.dto.TendererProjectDetailResponse;
import com.guandian.bidding.module.tender.dto.TendererProjectSummaryResponse;
import com.guandian.bidding.module.tender.service.TendererProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "招标人项目", description = "招标人查看关联项目(只读)")
@RestController
@RequestMapping("/api/tenderer/projects")
@RequiredArgsConstructor
public class TendererController {

    private final TendererProjectService tendererProjectService;

    @Operation(summary = "我的项目列表")
    @GetMapping
    @PreAuthorize("hasRole('TENDERER')")
    public R<PageResult<TendererProjectSummaryResponse>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(tendererProjectService.list(name, status, pageNum, pageSize));
    }

    @Operation(summary = "项目详情(只读)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TENDERER')")
    public R<TendererProjectDetailResponse> detail(@PathVariable Long id) {
        return R.ok(tendererProjectService.detail(id));
    }
}
