package com.guandian.bidding.module.bidder.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.bidder.dto.BidderProjectResponse;
import com.guandian.bidding.module.bidder.service.BidderService;
import com.guandian.bidding.module.manager.dto.RegistrationDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "投标人", description = "我的报名 / 我的项目")
@RestController
@RequestMapping("/api/bidder")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BIDDER')")
public class BidderController {

    private final BidderService bidderService;

    @Operation(summary = "我的报名")
    @GetMapping("/registrations")
    public R<PageResult<RegistrationDetailResponse>> myRegistrations(
            @RequestParam(required = false) String regStatus,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(bidderService.listMyRegistrations(regStatus, type, keyword, pageNum, pageSize));
    }

    @Operation(summary = "我的投标项目")
    @GetMapping("/projects")
    public R<PageResult<BidderProjectResponse>> myProjects(
            @RequestParam(required = false) String projectStatus,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(bidderService.listMyProjects(projectStatus, pageNum, pageSize));
    }
}
