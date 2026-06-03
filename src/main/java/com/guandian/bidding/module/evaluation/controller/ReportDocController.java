package com.guandian.bidding.module.evaluation.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.evaluation.dto.ReportDocItemResponse;
import com.guandian.bidding.module.evaluation.service.ManagerReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "专家-评标报告签名", description = "文档签名")
@RestController
@RequestMapping("/api/report-docs")
@RequiredArgsConstructor
public class ReportDocController {

    private final ManagerReportService reportService;

    @Operation(summary = "评标报告文档签名")
    @PostMapping("/{id}/sign")
    @PreAuthorize("hasRole('EXPERT')")
    public R<ReportDocItemResponse> sign(@PathVariable Long id) {
        return R.ok(reportService.signReportDoc(id));
    }
}
