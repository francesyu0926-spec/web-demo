package com.guandian.bidding.module.bidder.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.bidder.dto.RegistrationSubmitRequest;
import com.guandian.bidding.module.bidder.service.BidderService;
import com.guandian.bidding.module.manager.dto.RegistrationDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "投标报名", description = "提交报名")
@RestController
@RequestMapping("/api/tenders")
@RequiredArgsConstructor
public class TenderRegistrationController {

    private final BidderService bidderService;

    @Operation(summary = "提交报名")
    @PostMapping("/{id}/registrations")
    @PreAuthorize("hasRole('BIDDER')")
    public R<RegistrationDetailResponse> submit(@PathVariable Long id,
                                                @Validated @RequestBody RegistrationSubmitRequest request) {
        return R.ok(bidderService.submitRegistration(id, request));
    }
}
