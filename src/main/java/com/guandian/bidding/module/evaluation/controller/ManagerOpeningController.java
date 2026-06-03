package com.guandian.bidding.module.evaluation.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.evaluation.dto.*;
import com.guandian.bidding.module.evaluation.service.ManagerOpeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "项目经理-开评标", description = "开标推进 / 签到 / 解密")
@RestController
@RequestMapping("/api/manager/tenders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerOpeningController {

    private final ManagerOpeningService openingService;

    @Operation(summary = "发起开标(BIDDING→OPENING)")
    @PostMapping("/{id}/open")
    public R<ProjectProgressResponse> open(@PathVariable Long id) {
        return R.ok(openingService.openProject(id));
    }

    @Operation(summary = "查看开评标进度")
    @GetMapping("/{id}/progress")
    public R<ProjectProgressResponse> progress(@PathVariable Long id) {
        return R.ok(openingService.getProgress(id));
    }

    @Operation(summary = "专家签到情况")
    @GetMapping("/{id}/sign-in")
    public R<List<ExpertSignInItemResponse>> signInStatus(@PathVariable Long id) {
        return R.ok(openingService.getSignInStatus(id));
    }

    @Operation(summary = "确认专家签到，进入解密环节")
    @PostMapping("/{id}/sign-in/confirm")
    public R<ProjectProgressResponse> confirmSignIn(@PathVariable Long id) {
        return R.ok(openingService.confirmSignIn(id));
    }

    @Operation(summary = "投标文件解密情况")
    @GetMapping("/{id}/decrypt-status")
    public R<List<DecryptStatusItemResponse>> decryptStatus(@PathVariable Long id) {
        return R.ok(openingService.getDecryptStatus(id));
    }
}
