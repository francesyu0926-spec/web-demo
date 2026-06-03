package com.guandian.bidding.module.tender.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.tender.dto.TenderDetailResponse;
import com.guandian.bidding.module.tender.service.TenderPublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "招标项目", description = "游客可查看的招标详情")
@RestController
@RequestMapping("/api/tenders")
@RequiredArgsConstructor
public class TenderPublicController {

    private final TenderPublicService tenderPublicService;

    @Operation(summary = "招标项目详情")
    @GetMapping("/{id}")
    public R<TenderDetailResponse> detail(@PathVariable Long id) {
        return R.ok(tenderPublicService.getTenderDetail(id));
    }
}
