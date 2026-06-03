package com.guandian.bidding.module.content.service;

import com.guandian.bidding.module.content.dto.BidServiceResponse;
import com.guandian.bidding.security.LoginUser;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class HomeService {

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
