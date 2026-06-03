package com.guandian.bidding.module.user.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.user.dto.EnterpriseResponse;
import com.guandian.bidding.module.user.dto.EnterpriseUpdateRequest;
import com.guandian.bidding.module.user.dto.PreferenceResponse;
import com.guandian.bidding.module.user.dto.PreferenceUpdateRequest;
import com.guandian.bidding.module.user.dto.ProfileResponse;
import com.guandian.bidding.module.user.dto.ProfileUpdateRequest;
import com.guandian.bidding.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户", description = "个人资料 / 企业资料")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取个人资料")
    @GetMapping("/profile")
    public R<ProfileResponse> getProfile() {
        return R.ok(userService.getProfile());
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public R<ProfileResponse> updateProfile(@Validated @RequestBody ProfileUpdateRequest request) {
        return R.ok(userService.updateProfile(request));
    }

    @Operation(summary = "获取企业资料")
    @GetMapping("/enterprise")
    public R<EnterpriseResponse> getEnterprise() {
        return R.ok(userService.getEnterprise());
    }

    @Operation(summary = "保存企业资料")
    @PutMapping("/enterprise")
    public R<EnterpriseResponse> saveEnterprise(@Validated @RequestBody EnterpriseUpdateRequest request) {
        return R.ok(userService.saveEnterprise(request));
    }

    @Operation(summary = "我的偏好")
    @GetMapping("/preference")
    @PreAuthorize("hasRole('BIDDER')")
    public R<PreferenceResponse> getPreference() {
        return R.ok(userService.getPreference());
    }

    @Operation(summary = "更新我的偏好")
    @PutMapping("/preference")
    @PreAuthorize("hasRole('BIDDER')")
    public R<PreferenceResponse> savePreference(@RequestBody PreferenceUpdateRequest request) {
        return R.ok(userService.savePreference(request));
    }
}
