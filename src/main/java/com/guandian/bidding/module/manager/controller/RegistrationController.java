package com.guandian.bidding.module.manager.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.bidder.dto.*;
import com.guandian.bidding.module.bidder.service.BidderService;
import com.guandian.bidding.module.manager.dto.*;
import com.guandian.bidding.module.manager.service.ManagerRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "报名", description = "报名详情 / 审核 / 缴费")
@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final ManagerRegistrationService registrationService;
    private final BidderService bidderService;

    @Operation(summary = "报名详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','BIDDER')")
    public R<RegistrationDetailResponse> detail(@PathVariable Long id) {
        return R.ok(registrationService.getDetail(id));
    }

    @Operation(summary = "报名审核")
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('MANAGER')")
    public R<RegistrationDetailResponse> audit(@PathVariable Long id,
                                               @Validated @RequestBody RegistrationAuditRequest request) {
        return R.ok(registrationService.audit(id, request));
    }

    @Operation(summary = "查看缴费信息(项目经理)")
    @GetMapping("/{id}/payment")
    @PreAuthorize("hasRole('MANAGER')")
    public R<RegistrationPaymentResponse> payment(@PathVariable Long id) {
        return R.ok(registrationService.getPayment(id));
    }

    @Operation(summary = "取消报名")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('BIDDER')")
    public R<RegistrationDetailResponse> cancel(@PathVariable Long id) {
        return R.ok(bidderService.cancelRegistration(id));
    }

    @Operation(summary = "报名缴费")
    @PostMapping("/{id}/payment")
    @PreAuthorize("hasRole('BIDDER')")
    public R<PaymentOrderResponse> pay(@PathVariable Long id,
                                       @Validated @RequestBody PaymentRequest request) {
        return R.ok(bidderService.payRegistration(id, request));
    }

    @Operation(summary = "查看账单")
    @GetMapping("/{id}/bill")
    @PreAuthorize("hasRole('BIDDER')")
    public R<PaymentOrderResponse> bill(@PathVariable Long id) {
        return R.ok(bidderService.getBill(id));
    }

    @Operation(summary = "递交投标文件")
    @PostMapping("/{id}/bid-document")
    @PreAuthorize("hasRole('BIDDER')")
    public R<BidDocumentResponse> submitBidDocument(@PathVariable Long id,
                                                  @Validated @RequestBody BidDocumentSubmitRequest request) {
        return R.ok(bidderService.submitBidDocument(id, request));
    }

    @Operation(summary = "投标详情")
    @GetMapping("/{id}/bid-detail")
    @PreAuthorize("hasRole('BIDDER')")
    public R<BidDetailResponse> bidDetail(@PathVariable Long id) {
        return R.ok(bidderService.getBidDetail(id));
    }

    @Operation(summary = "缴纳代理费")
    @PostMapping("/{id}/agency-fee/pay")
    @PreAuthorize("hasRole('BIDDER')")
    public R<PaymentOrderResponse> payAgencyFee(@PathVariable Long id,
                                                @Validated @RequestBody PaymentRequest request) {
        return R.ok(bidderService.payAgencyFee(id, request));
    }
}
