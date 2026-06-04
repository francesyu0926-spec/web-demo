package com.guandian.bidding.module.auth.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.auth.dto.TendererInviteCreateRequest;
import com.guandian.bidding.module.auth.dto.TendererInviteRespondRequest;
import com.guandian.bidding.module.auth.dto.TendererInviteResponse;
import com.guandian.bidding.module.auth.service.TendererInviteService;
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

@Tag(name = "招标人邀请", description = "项目经理邀请招标人")
@RestController
@RequestMapping("/api/tenderer-invites")
@RequiredArgsConstructor
public class TendererInviteController {

    private final TendererInviteService inviteService;

    @Operation(summary = "邀请招标人")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public R<TendererInviteResponse> create(@Validated @RequestBody TendererInviteCreateRequest request) {
        return R.ok(inviteService.create(request));
    }

    @Operation(summary = "我收到的招标人邀请")
    @GetMapping("/received")
    public R<PageResult<TendererInviteResponse>> listReceived(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(inviteService.listReceived(status, pageNum, pageSize));
    }

    @Operation(summary = "接受/拒绝招标人邀请")
    @PutMapping("/{id}/respond")
    public R<TendererInviteResponse> respond(@PathVariable Long id,
                                             @Validated @RequestBody TendererInviteRespondRequest request) {
        return R.ok(inviteService.respond(id, request));
    }
}
