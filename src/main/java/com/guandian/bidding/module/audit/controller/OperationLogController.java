package com.guandian.bidding.module.audit.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.audit.dto.OperationLogResponse;
import com.guandian.bidding.module.audit.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "操作审计", description = "操作日志查询")
@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "操作日志列表(管理员)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<OperationLogResponse>> list(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long bizId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(operationLogService.listForAdmin(module, action, bizId, userId, pageNum, pageSize));
    }
}
