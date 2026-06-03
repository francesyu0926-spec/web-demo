package com.guandian.bidding.module.tender.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.tender.dto.WinnerDetailResponse;
import com.guandian.bidding.module.tender.service.TenderPublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "中标公示", description = "游客可查看的中标详情")
@RestController
@RequestMapping("/api/winners")
@RequiredArgsConstructor
public class WinnerPublicController {

    private final TenderPublicService tenderPublicService;

    @Operation(summary = "中标详情")
    @GetMapping("/{id}")
    public R<WinnerDetailResponse> detail(@PathVariable Long id) {
        return R.ok(tenderPublicService.getWinnerDetail(id));
    }
}
