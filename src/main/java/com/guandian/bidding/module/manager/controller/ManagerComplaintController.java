package com.guandian.bidding.module.manager.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.content.dto.ComplaintFeedbackRequest;
import com.guandian.bidding.module.content.dto.ComplaintResponse;
import com.guandian.bidding.module.content.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "项目经理-投诉处理", description = "投诉意见查看与反馈")
@RestController
@RequestMapping("/api/manager/complaints")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerComplaintController {

    private final ComplaintService complaintService;

    @Operation(summary = "投诉意见列表")
    @GetMapping
    public R<PageResult<ComplaintResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(complaintService.listForManager(keyword, status, pageNum, pageSize));
    }

    @Operation(summary = "投诉处理反馈")
    @PutMapping("/{id}/feedback")
    public R<ComplaintResponse> feedback(@PathVariable Long id,
                                         @Validated @RequestBody ComplaintFeedbackRequest request) {
        return R.ok(complaintService.feedback(id, request.getReply()));
    }
}
