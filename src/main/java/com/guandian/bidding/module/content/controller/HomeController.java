package com.guandian.bidding.module.content.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.content.dto.BannerItemResponse;
import com.guandian.bidding.module.content.dto.BidServiceResponse;
import com.guandian.bidding.module.content.dto.SiteLinkResponse;
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

import java.util.List;

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

    @Operation(summary = "招标/非招标方式采购列表")
    @GetMapping("/tenders")
    public R<PageResult<AnnouncementItemResponse>> homeTenders(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tenderType,
            @RequestParam(required = false) String procurementType,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(tenderPublicService.listHomeTenders(category, tenderType, procurementType, pageNum, pageSize));
    }

    @Operation(summary = "广告轮播")
    @GetMapping("/banners")
    public R<List<BannerItemResponse>> banners() {
        return R.ok(homeService.listBanners());
    }

    @Operation(summary = "常用网站链接")
    @GetMapping("/links")
    public R<List<SiteLinkResponse>> links() {
        return R.ok(homeService.listSiteLinks());
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
