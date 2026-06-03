package com.guandian.bidding.module.evaluation.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.evaluation.dto.NegotiationReplyRequest;
import com.guandian.bidding.module.evaluation.dto.NegotiationItemResponse;
import com.guandian.bidding.module.evaluation.dto.SecondQuoteItemResponse;
import com.guandian.bidding.module.evaluation.dto.SecondQuoteReplyRequest;
import com.guandian.bidding.module.evaluation.service.NegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "投标人-谈判磋商", description = "谈判 / 二轮报价回复")
@RestController
@RequiredArgsConstructor
public class NegotiationBidderController {

    private final NegotiationService negotiationService;

    @Operation(summary = "我收到的谈判磋商")
    @GetMapping("/api/registrations/{id}/negotiations")
    @PreAuthorize("hasRole('BIDDER')")
    public R<List<NegotiationItemResponse>> listNegotiations(@PathVariable Long id) {
        return R.ok(negotiationService.listNegotiations(id));
    }

    @Operation(summary = "谈判磋商回复")
    @PostMapping("/api/negotiations/{id}/reply")
    @PreAuthorize("hasRole('BIDDER')")
    public R<NegotiationItemResponse> replyNegotiation(@PathVariable Long id,
                                                       @RequestBody NegotiationReplyRequest request) {
        return R.ok(negotiationService.replyNegotiation(id, request));
    }

    @Operation(summary = "我收到的二轮报价")
    @GetMapping("/api/registrations/{id}/second-quotes")
    @PreAuthorize("hasRole('BIDDER')")
    public R<List<SecondQuoteItemResponse>> listSecondQuotes(@PathVariable Long id) {
        return R.ok(negotiationService.listSecondQuotes(id));
    }

    @Operation(summary = "二轮报价回复")
    @PostMapping("/api/second-quotes/{id}/reply")
    @PreAuthorize("hasRole('BIDDER')")
    public R<SecondQuoteItemResponse> replySecondQuote(@PathVariable Long id,
                                                       @RequestBody SecondQuoteReplyRequest request) {
        return R.ok(negotiationService.replySecondQuote(id, request));
    }
}
