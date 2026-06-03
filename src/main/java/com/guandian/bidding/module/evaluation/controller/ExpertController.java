package com.guandian.bidding.module.evaluation.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.evaluation.dto.*;
import com.guandian.bidding.module.evaluation.service.ExpertEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "专家-开评标", description = "任务列表 / 确认 / 签到 / 审查 / 评分")
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

    @Operation(summary = "符合性审查(形式/资格/响应性)")
    @PostMapping("/api/expert/reviews")
    @PreAuthorize("hasRole('EXPERT')")
    public R<ComplianceResultResponse> submitReviews(@Validated @RequestBody ExpertReviewRequest request) {
        return R.ok(expertService.submitReviews(request));
    }

    @Operation(summary = "商务/技术/报价评分")
    @PostMapping("/api/expert/scores")
    @PreAuthorize("hasRole('EXPERT')")
    public R<ScoreSummaryResponse> submitScores(@Validated @RequestBody ExpertScoreSubmitRequest request) {
        return R.ok(expertService.submitScores(request));
    }

    @Operation(summary = "最终得分及候选人")
    @GetMapping("/api/expert/tenders/{id}/final-score")
    @PreAuthorize("hasRole('EXPERT')")
    public R<FinalScoreResponse> finalScore(@PathVariable Long id) {
        return R.ok(expertService.getFinalScore(id));
    }

    @Operation(summary = "推选专家组长")
    @PostMapping("/api/expert/tenders/{id}/elect-leader")
    @PreAuthorize("hasRole('EXPERT')")
    public R<List<ExpertAssignmentItemResponse>> electLeader(@PathVariable Long id,
                                                            @Validated @RequestBody ElectLeaderRequest request) {
        return R.ok(expertService.electLeader(id, request));
    }
}
