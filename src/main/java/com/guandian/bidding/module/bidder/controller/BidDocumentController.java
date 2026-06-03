package com.guandian.bidding.module.bidder.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.bidder.dto.BidDocumentPwdRequest;
import com.guandian.bidding.module.bidder.dto.BidDocumentResponse;
import com.guandian.bidding.module.bidder.service.BidderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "投标文件", description = "加密 / 撤回 / 解密")
@RestController
@RequestMapping("/api/bid-documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BIDDER')")
public class BidDocumentController {

    private final BidderService bidderService;

    @Operation(summary = "文件加密")
    @PostMapping("/{id}/encrypt")
    public R<BidDocumentResponse> encrypt(@PathVariable Long id,
                                        @Validated @RequestBody BidDocumentPwdRequest request) {
        return R.ok(bidderService.encryptBidDocument(id, request));
    }

    @Operation(summary = "撤回投标文件")
    @PostMapping("/{id}/withdraw")
    public R<BidDocumentResponse> withdraw(@PathVariable Long id) {
        return R.ok(bidderService.withdrawBidDocument(id));
    }

    @Operation(summary = "签字解密")
    @PostMapping("/{id}/decrypt")
    public R<BidDocumentResponse> decrypt(@PathVariable Long id,
                                          @Validated @RequestBody BidDocumentPwdRequest request) {
        return R.ok(bidderService.decryptBidDocument(id, request));
    }
}
