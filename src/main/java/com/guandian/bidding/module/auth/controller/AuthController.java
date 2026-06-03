package com.guandian.bidding.module.auth.controller;

import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.auth.dto.AuthTokenResponse;
import com.guandian.bidding.module.auth.dto.CurrentUserResponse;
import com.guandian.bidding.module.auth.dto.LoginRequest;
import com.guandian.bidding.module.auth.dto.RegisterRequest;
import com.guandian.bidding.module.auth.dto.SmsCodeRequest;
import com.guandian.bidding.module.auth.dto.SwitchRoleRequest;
import com.guandian.bidding.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证", description = "注册 / 登录 / 验证码")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<AuthTokenResponse> register(@Validated @RequestBody RegisterRequest request) {
        return R.ok(authService.register(request));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<AuthTokenResponse> login(@Validated @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms-code")
    public R<Void> sendSmsCode(@Validated @RequestBody SmsCodeRequest request) {
        authService.sendSmsCode(request);
        return R.ok();
    }

    @Operation(summary = "当前登录用户信息")
    @GetMapping("/me")
    public R<CurrentUserResponse> me() {
        return R.ok(authService.me());
    }

    @Operation(summary = "切换激活角色")
    @PostMapping("/switch-role")
    public R<AuthTokenResponse> switchRole(@Validated @RequestBody SwitchRoleRequest request) {
        return R.ok(authService.switchRole(request));
    }
}
