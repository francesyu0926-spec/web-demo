package com.guandian.bidding.module.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.module.content.dto.BannerItemResponse;
import com.guandian.bidding.module.content.dto.BidServiceResponse;
import com.guandian.bidding.module.content.dto.SiteLinkResponse;
import com.guandian.bidding.module.content.entity.CmsBanner;
import com.guandian.bidding.module.content.entity.CmsSiteLink;
import com.guandian.bidding.module.content.mapper.CmsBannerMapper;
import com.guandian.bidding.module.content.mapper.CmsSiteLinkMapper;
import com.guandian.bidding.security.LoginUser;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final CmsBannerMapper bannerMapper;
    private final CmsSiteLinkMapper siteLinkMapper;

    public List<BannerItemResponse> listBanners() {
        return bannerMapper.selectList(new LambdaQueryWrapper<CmsBanner>()
                        .eq(CmsBanner::getStatus, 1)
                        .orderByAsc(CmsBanner::getSort)
                        .orderByDesc(CmsBanner::getId))
                .stream()
                .map(b -> BannerItemResponse.builder()
                        .id(b.getId())
                        .title(b.getTitle())
                        .imageId(b.getImageId())
                        .linkUrl(b.getLinkUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SiteLinkResponse> listSiteLinks() {
        return siteLinkMapper.selectList(new LambdaQueryWrapper<CmsSiteLink>()
                        .eq(CmsSiteLink::getStatus, 1)
                        .orderByAsc(CmsSiteLink::getSort)
                        .orderByDesc(CmsSiteLink::getId))
                .stream()
                .map(l -> SiteLinkResponse.builder()
                        .id(l.getId())
                        .name(l.getName())
                        .url(l.getUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public BidServiceResponse checkBidService() {
        LoginUser user = SecurityUtils.requireLoginUser();
        if ("BIDDER".equals(user.getActiveRole())) {
            return BidServiceResponse.builder()
                    .allowed(true)
                    .needSwitchRole(false)
                    .message("已进入投标服务")
                    .build();
        }
        return BidServiceResponse.builder()
                .allowed(false)
                .needSwitchRole(true)
                .message("请先切换为投标人身份")
                .build();
    }
}
