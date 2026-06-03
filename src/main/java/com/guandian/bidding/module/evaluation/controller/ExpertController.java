package com.guandian.bidding.module.evaluation.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.evaluation.dto.AssignmentRespondRequest;
import com.guandian.bidding.module.evaluation.dto.ExpertAssignmentItemResponse;
import com.guandian.bidding.module.evaluation.service.ExpertEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "专家-开评标", description = "任务列表 / 确认 / 签到")
@RestController
@RequiredArgsConstructor
public class ExpertController {

    private final ExpertEvaluationService expertService;

    @Operation(summary = "我的评标任务")
    @GetMapping("/api/expert/assignments")
    @PreAuthorize("hasRole('EXPERT')")
    public R<List<ExpertAssignmentItemResponse>> assignments(@RequestParam(required = false) String status) {
        return R.ok(expertService.listAssignments(status));
    }

    @Operation(summary = "确认/拒绝评标邀请")
    @PostMapping("/api/assignments/{id}/respond")
    @PreAuthorize("hasRole('EXPERT')")
    public R<ExpertAssignmentItemResponse> respond(@PathVariable Long id,
                                                    @Validated @RequestBody AssignmentRespondRequest request) {
        return R.ok(expertService.respond(id, request));
    }

    @Operation(summary = "专家签到")
    @PostMapping("/api/assignments/{id}/sign-in")
    @PreAuthorize("hasRole('EXPERT')")
    public R<ExpertAssignmentItemResponse> signIn(@PathVariable Long id) {
        return R.ok(expertService.signIn(id));
    }
}
