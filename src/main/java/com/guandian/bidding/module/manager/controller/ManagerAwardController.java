package com.guandian.bidding.module.manager.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.manager.dto.*;
import com.guandian.bidding.module.manager.service.ManagerAwardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "项目经理-中标定标", description = "中标公示 / 代理费 / 通知书 / 归档")
@RestController
@RequestMapping("/api/manager/tenders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerAwardController {

    private final ManagerAwardService awardService;

    @Operation(summary = "发布中标公示")
    @PostMapping("/{id}/award")
    public R<Void> publishAward(@PathVariable Long id,
                                @Validated @RequestBody AwardPublishRequest request) {
        awardService.publishAward(id, request);
        return R.ok();
    }

    @Operation(summary = "推送代理费")
    @PostMapping("/{id}/agency-fee/push")
    public R<Void> pushAgencyFee(@PathVariable Long id,
                                 @Validated @RequestBody AgencyFeePushRequest request) {
        awardService.pushAgencyFee(id, request);
        return R.ok();
    }

    @Operation(summary = "修改代理费")
    @PutMapping("/{id}/agency-fee")
    public R<Void> updateAgencyFee(@PathVariable Long id,
                                   @Validated @RequestBody AgencyFeeUpdateRequest request) {
        awardService.updateAgencyFee(id, request);
        return R.ok();
    }

    @Operation(summary = "发布中标通知书")
    @PostMapping("/{id}/notice")
    public R<Void> publishNotice(@PathVariable Long id,
                                 @RequestBody NoticePublishRequest request) {
        awardService.publishNotice(id, request);
        return R.ok();
    }

    @Operation(summary = "项目归档")
    @PostMapping("/{id}/archive")
    public R<Void> archive(@PathVariable Long id) {
        awardService.archiveProject(id);
        return R.ok();
    }
}
