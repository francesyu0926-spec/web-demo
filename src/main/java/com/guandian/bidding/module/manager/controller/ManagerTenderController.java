package com.guandian.bidding.module.manager.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.manager.dto.*;
import com.guandian.bidding.module.manager.service.ManagerExpertService;
import com.guandian.bidding.module.manager.service.ManagerRegistrationService;
import com.guandian.bidding.module.manager.service.ManagerTenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "项目经理-项目", description = "招标项目管理")
@RestController
@RequestMapping("/api/manager/tenders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerTenderController {

    private final ManagerTenderService tenderService;
    private final ManagerExpertService expertService;
    private final ManagerRegistrationService registrationService;

    @Operation(summary = "我的项目列表")
    @GetMapping
    public R<PageResult<ManagerTenderSummaryResponse>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String projectNo,
            @RequestParam(required = false) String tenderType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bidOpenFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime bidOpenTo,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(tenderService.list(name, projectNo, tenderType, status, bidOpenFrom, bidOpenTo, pageNum, pageSize));
    }

    @Operation(summary = "项目详情")
    @GetMapping("/{id}")
    public R<ManagerTenderDetailResponse> detail(@PathVariable Long id) {
        return R.ok(tenderService.detail(id));
    }

    @Operation(summary = "创建并发布项目")
    @PostMapping
    public R<ManagerTenderDetailResponse> create(@Validated @RequestBody ManagerTenderCreateRequest request) {
        return R.ok(tenderService.create(request));
    }

    @Operation(summary = "发布变更")
    @PutMapping("/{id}")
    public R<ManagerTenderDetailResponse> update(@PathVariable Long id,
                                                 @RequestBody ManagerTenderUpdateRequest request) {
        return R.ok(tenderService.update(id, request));
    }

    @Operation(summary = "废标")
    @PostMapping("/{id}/abort")
    public R<ManagerTenderDetailResponse> abort(@PathVariable Long id,
                                              @Validated @RequestBody AbortTenderRequest request) {
        return R.ok(tenderService.abort(id, request));
    }

    @Operation(summary = "跳过评审")
    @PostMapping("/{id}/skip-eval")
    public R<ManagerTenderDetailResponse> skipEval(@PathVariable Long id) {
        return R.ok(tenderService.skipEval(id));
    }

    @Operation(summary = "编辑评审表")
    @PutMapping("/{id}/eval-items")
    public R<ManagerTenderDetailResponse> updateEvalItems(@PathVariable Long id,
                                                          @RequestBody EvalItemsUpdateRequest request) {
        return R.ok(tenderService.updateEvalItems(id, request));
    }

    @Operation(summary = "报名审核列表")
    @GetMapping("/{id}/registrations")
    public R<List<RegistrationSummaryResponse>> registrations(@PathVariable Long id) {
        return R.ok(registrationService.listByProject(id));
    }

    @Operation(summary = "评标专家列表")
    @GetMapping("/{id}/experts")
    public R<List<ExpertAssignmentResponse>> experts(@PathVariable Long id) {
        return R.ok(expertService.listExperts(id));
    }

    @Operation(summary = "移除专家")
    @DeleteMapping("/{id}/experts/{eid}")
    public R<Void> removeExpert(@PathVariable Long id, @PathVariable Long eid) {
        expertService.removeExpert(id, eid);
        return R.ok();
    }

    @Operation(summary = "抽取/邀请专家")
    @PostMapping("/{id}/experts/draw")
    public R<List<ExpertAssignmentResponse>> drawExperts(@PathVariable Long id,
                                                         @RequestBody ExpertDrawRequest request) {
        return R.ok(expertService.drawExperts(id, request));
    }
}
