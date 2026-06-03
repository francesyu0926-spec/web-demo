package com.guandian.bidding.module.auth.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.auth.dto.RoleApplicationAuditRequest;
import com.guandian.bidding.module.auth.dto.RoleApplicationRequest;
import com.guandian.bidding.module.auth.dto.RoleApplicationResponse;
import com.guandian.bidding.module.auth.service.RoleApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "角色申请", description = "申请成为项目经理/专家，管理员审核")
@RestController
@RequestMapping("/api/role-applications")
@RequiredArgsConstructor
public class RoleApplicationController {

    private final RoleApplicationService roleApplicationService;

    @Operation(summary = "提交角色申请")
    @PostMapping
    public R<RoleApplicationResponse> apply(@Validated @RequestBody RoleApplicationRequest request) {
        return R.ok(roleApplicationService.apply(request));
    }

    @Operation(summary = "角色申请审核列表（管理员）")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public R<PageResult<RoleApplicationResponse>> list(
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(roleApplicationService.listForAdmin(auditStatus, pageNum, pageSize));
    }

    @Operation(summary = "审核角色申请（管理员）")
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public R<RoleApplicationResponse> audit(@PathVariable Long id,
                                            @Validated @RequestBody RoleApplicationAuditRequest request) {
        return R.ok(roleApplicationService.audit(id, request));
    }
}
