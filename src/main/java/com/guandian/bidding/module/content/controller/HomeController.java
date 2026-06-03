package com.guandian.bidding.module.content.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.content.dto.BidServiceResponse;
import com.guandian.bidding.module.content.service.HomeService;
import com.guandian.bidding.module.tender.dto.AnnouncementItemResponse;
import com.guandian.bidding.module.tender.dto.WinnerItemResponse;
import com.guandian.bidding.module.tender.service.TenderPublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "首页", description = "招标公告 / 中标公示")
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final TenderPublicService tenderPublicService;
    private final HomeService homeService;

    @Operation(summary = "首页招标公告")
    @GetMapping("/announcements")
    public R<PageResult<AnnouncementItemResponse>> announcements(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(tenderPublicService.listAnnouncements(pageNum, pageSize));
    }

    @Operation(summary = "首页中标公示")
    @GetMapping("/winners")
    public R<PageResult<WinnerItemResponse>> winners(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(tenderPublicService.listWinners(pageNum, pageSize));
    }

    @Operation(summary = "投标服务入口")
    @PostMapping("/bid-service")
    public R<BidServiceResponse> bidService() {
        return R.ok(homeService.checkBidService());
    }
}
