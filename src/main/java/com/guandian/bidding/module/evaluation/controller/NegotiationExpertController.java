package com.guandian.bidding.module.evaluation.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.evaluation.dto.*;
import com.guandian.bidding.module.evaluation.service.NegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "专家-谈判磋商", description = "谈判 / 二轮报价发起")
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('EXPERT')")
public class NegotiationExpertController {

    private final NegotiationService negotiationService;

    @Operation(summary = "组长发起谈判磋商")
    @PostMapping("/api/expert/negotiations")
    public R<NegotiationItemResponse> createNegotiation(@Validated @RequestBody NegotiationCreateRequest request) {
        return R.ok(negotiationService.createNegotiation(request));
    }

    @Operation(summary = "组长发起二轮报价")
    @PostMapping("/api/expert/second-quotes")
    public R<SecondQuoteItemResponse> createSecondQuote(@Validated @RequestBody SecondQuoteCreateRequest request) {
        return R.ok(negotiationService.createSecondQuote(request));
    }
}
