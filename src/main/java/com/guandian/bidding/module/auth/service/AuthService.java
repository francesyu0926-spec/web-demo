package com.guandian.bidding.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.auth.dto.AuthTokenResponse;
import com.guandian.bidding.module.auth.dto.CurrentUserResponse;
import com.guandian.bidding.module.auth.dto.LoginRequest;
import com.guandian.bidding.module.auth.dto.RegisterRequest;
import com.guandian.bidding.module.auth.dto.SmsCodeRequest;
import com.guandian.bidding.module.auth.dto.SwitchRoleRequest;
import com.guandian.bidding.module.auth.dto.WechatLoginRequest;
import com.guandian.bidding.security.SecurityUtils;
import com.guandian.bidding.module.auth.entity.SysRole;
import com.guandian.bidding.module.auth.entity.SysUser;
import com.guandian.bidding.module.auth.entity.SysUserRole;
import com.guandian.bidding.module.auth.mapper.SysRoleMapper;
import com.guandian.bidding.module.auth.mapper.SysUserMapper;
import com.guandian.bidding.module.auth.mapper.SysUserRoleMapper;
import com.guandian.bidding.security.JwtTokenProvider;
import com.guandian.bidding.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_ROLE = "BIDDER";
    private static final String SMS_KEY_PREFIX = "sms:code:";
    private static final Duration SMS_TTL = Duration.ofMinutes(5);

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Transactional(rollbackFor = Exception.class)
    public AuthTokenResponse register(RegisterRequest req) {
        String username = StringUtils.hasText(req.getUsername()) ? req.getUsername() : req.getPhone();
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名和手机号至少填写一项");
        }
        if (StringUtils.hasText(req.getPhone())) {
            if (!StringUtils.hasText(req.getSmsCode())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "填写手机号时必须提供短信验证码");
            }
            verifySmsCode(req.getPhone(), req.getSmsCode());
        }

        if (existsByUsername(username)) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS, "用户名已存在");
        }
        if (StringUtils.hasText(req.getPhone()) && existsByPhone(req.getPhone())) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS, "手机号已注册");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setPhone(req.getPhone());
        user.setRealName(req.getRealName());
        user.setStatus(1);
        userMapper.insert(user);

        assignRole(user.getId(), DEFAULT_ROLE);
        return buildTokenResponse(user);
    }

    public AuthTokenResponse login(LoginRequest req) {
        String loginType = req.getLoginType().toUpperCase();
        SysUser user;
        switch (loginType) {
            case "PASSWORD":
                user = loginByPassword(req.getAccount(), req.getCredential());
                break;
            case "SMS":
                verifySmsCode(req.getAccount(), req.getCredential());
                user = findByAccount(req.getAccount());
                if (user == null) {
                    throw new BusinessException(ResultCode.LOGIN_FAILED, "账号不存在");
                }
                break;
            case "WECHAT":
                throw new BusinessException(ResultCode.PARAM_ERROR, "微信登录暂未开放");
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的登录方式: " + loginType);
        }
        ensureUserEnabled(user);
        return buildTokenResponse(user);
    }

    public AuthTokenResponse wechatLogin(WechatLoginRequest req) {
        throw new BusinessException(ResultCode.PARAM_ERROR, "微信登录暂未开放，请使用账号或短信登录");
    }

    public void sendSmsCode(SmsCodeRequest req) {
        String scene = req.getScene().toUpperCase();
        if (!"REGISTER".equals(scene) && !"LOGIN".equals(scene)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "scene 仅支持 REGISTER 或 LOGIN");
        }
        if ("LOGIN".equals(scene) && findByPhone(req.getPhone()) == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED, "该手机号尚未注册");
        }
        if ("REGISTER".equals(scene) && existsByPhone(req.getPhone())) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS, "手机号已注册");
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        String key = smsKey(req.getPhone());
        try {
            redisTemplate.opsForValue().set(key, code, SMS_TTL);
        } catch (Exception e) {
            log.error("Redis 不可用，无法发送验证码", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "短信服务暂不可用，请稍后再试");
        }
        // 开发环境：验证码写入日志，便于联调
        log.info("[DEV] 短信验证码 phone={} scene={} code={}", req.getPhone(), scene, code);
    }

    public CurrentUserResponse me() {
        LoginUser loginUser = SecurityUtils.requireLoginUser();
        SysUser user = requireUser(loginUser.getUserId());
        List<String> roles = listApprovedRoleCodes(user.getId());
        return CurrentUserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(roles)
                .activeRole(loginUser.getActiveRole())
                .build();
    }

    public AuthTokenResponse switchRole(SwitchRoleRequest req) {
        LoginUser loginUser = SecurityUtils.requireLoginUser();
        String roleCode = req.getRole().toUpperCase();
        List<String> roles = listApprovedRoleCodes(loginUser.getUserId());
        if (!roles.contains(roleCode)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您没有该角色或未通过审核");
        }
        SysUser user = requireUser(loginUser.getUserId());
        return buildTokenResponse(user, roleCode);
    }

    private SysUser loginByPassword(String account, String rawPassword) {
        SysUser user = findByAccount(account);
        if (user == null || !StringUtils.hasText(user.getPassword())
                || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        return user;
    }

    private void verifySmsCode(String phone, String code) {
        String key = smsKey(phone);
        String cached;
        try {
            cached = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis 不可用，无法校验验证码", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "短信服务暂不可用，请稍后再试");
        }
        if (!StringUtils.hasText(cached) || !cached.equals(code)) {
            throw new BusinessException(ResultCode.SMS_CODE_INVALID);
        }
        redisTemplate.delete(key);
    }

    private AuthTokenResponse buildTokenResponse(SysUser user) {
        List<String> roles = listApprovedRoleCodes(user.getId());
        String activeRole = roles.isEmpty() ? "GUEST" : roles.get(0);
        return buildTokenResponse(user, activeRole);
    }

    private AuthTokenResponse buildTokenResponse(SysUser user, String activeRole) {
        List<String> roles = listApprovedRoleCodes(user.getId());
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), activeRole);
        String token = tokenProvider.generateToken(loginUser);
        return AuthTokenResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .roles(roles)
                .activeRole(activeRole)
                .build();
    }

    private SysUser requireUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        ensureUserEnabled(user);
        return user;
    }

    private List<String> listApprovedRoleCodes(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getAuditStatus, 1));
        if (userRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds))
                .stream()
                .map(SysRole::getCode)
                .collect(Collectors.toList());
    }

    private void assignRole(Long userId, String roleCode) {
        SysRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode));
        if (role == null) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "默认角色未配置: " + roleCode);
        }
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRole.setAuditStatus(1);
        userRoleMapper.insert(userRole);
    }

    private void ensureUserEnabled(SysUser user) {
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
    }

    private SysUser findByAccount(String account) {
        SysUser user = findByUsername(account);
        if (user == null) {
            user = findByPhone(account);
        }
        return user;
    }

    private SysUser findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    private SysUser findByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));
    }

    private boolean existsByUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)) > 0;
    }

    private boolean existsByPhone(String phone) {
        return userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone)) > 0;
    }

    private String smsKey(String phone) {
        return SMS_KEY_PREFIX + phone;
    }
}
