package com.guandian.bidding.module.evaluation.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.evaluation.dto.*;
import com.guandian.bidding.module.evaluation.service.ManagerReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "项目经理-评标报告", description = "编辑 / 推送 / 导出")
@RestController
@RequestMapping("/api/manager/tenders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerReportController {

    private final ManagerReportService reportService;

    @Operation(summary = "评标报告详情")
    @GetMapping("/{id}/report")
    public R<ReportSummaryResponse> getReport(@PathVariable Long id) {
        return R.ok(reportService.getReport(id));
    }

    @Operation(summary = "编辑评标报告")
    @PutMapping("/{id}/report")
    public R<ReportSummaryResponse> updateReport(@PathVariable Long id,
                                                 @RequestBody ReportUpdateRequest request) {
        return R.ok(reportService.updateReport(id, request));
    }

    @Operation(summary = "推送评标报告至专家")
    @PostMapping("/{id}/report/push")
    public R<ReportSummaryResponse> pushReport(@PathVariable Long id) {
        return R.ok(reportService.pushReport(id));
    }

    @Operation(summary = "一键导出评标报告")
    @GetMapping("/{id}/report/export")
    public R<ReportExportResponse> exportReport(@PathVariable Long id) {
        return R.ok(reportService.exportReport(id));
    }
}
