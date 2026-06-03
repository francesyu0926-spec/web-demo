package com.guandian.bidding.module.manager.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.manager.dto.ParseTenderRequest;
import com.guandian.bidding.module.manager.dto.ParseTenderResponse;
import com.guandian.bidding.module.manager.service.ManagerTenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "文件", description = "招标文件解析")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class ParseTenderController {

    private final ManagerTenderService tenderService;

    @Operation(summary = "解析招标文件(占位)")
    @PostMapping("/parse-tender")
    @PreAuthorize("hasRole('MANAGER')")
    public R<ParseTenderResponse> parseTender(@Validated @RequestBody ParseTenderRequest request) {
        return R.ok(tenderService.parseTender(request));
    }
}
