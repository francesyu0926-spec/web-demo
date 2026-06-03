package com.guandian.bidding.module.content.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.content.dto.ComplaintCreateRequest;
import com.guandian.bidding.module.content.dto.ComplaintResponse;
import com.guandian.bidding.module.content.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "投诉建议", description = "我的投诉建议")
@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @Operation(summary = "我的投诉建议列表")
    @GetMapping
    public R<PageResult<ComplaintResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(complaintService.list(keyword, startTime, endTime, pageNum, pageSize));
    }

    @Operation(summary = "提交投诉建议")
    @PostMapping
    public R<ComplaintResponse> create(@Validated @RequestBody ComplaintCreateRequest request) {
        return R.ok(complaintService.create(request));
    }

    @Operation(summary = "投诉建议详情")
    @GetMapping("/{id}")
    public R<ComplaintResponse> detail(@PathVariable Long id) {
        return R.ok(complaintService.getDetail(id));
    }
}
