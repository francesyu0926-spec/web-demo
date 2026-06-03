package com.guandian.bidding.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.auth.entity.SysRole;
import com.guandian.bidding.module.auth.entity.SysUser;
import com.guandian.bidding.module.auth.entity.SysUserRole;
import com.guandian.bidding.module.auth.mapper.SysRoleMapper;
import com.guandian.bidding.module.auth.mapper.SysUserMapper;
import com.guandian.bidding.module.auth.mapper.SysUserRoleMapper;
import com.guandian.bidding.module.user.dto.EnterpriseResponse;
import com.guandian.bidding.module.user.dto.EnterpriseUpdateRequest;
import com.guandian.bidding.module.user.dto.ProfileResponse;
import com.guandian.bidding.module.user.dto.ProfileUpdateRequest;
import com.guandian.bidding.module.user.entity.SupplierProfile;
import com.guandian.bidding.module.user.mapper.SupplierProfileMapper;
import com.guandian.bidding.security.LoginUser;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SupplierProfileMapper supplierProfileMapper;

    public ProfileResponse getProfile() {
        LoginUser loginUser = SecurityUtils.requireLoginUser();
        SysUser user = requireUser(loginUser.getUserId());
        return toProfileResponse(user, loginUser.getActiveRole());
    }

    @Transactional(rollbackFor = Exception.class)
    public ProfileResponse updateProfile(ProfileUpdateRequest req) {
        LoginUser loginUser = SecurityUtils.requireLoginUser();
        SysUser user = requireUser(loginUser.getUserId());

        if (StringUtils.hasText(req.getPhone()) && !req.getPhone().equals(user.getPhone())
                && existsByPhone(req.getPhone(), user.getId())) {
            throw new BusinessException(ResultCode.ACCOUNT_EXISTS, "手机号已被使用");
        }

        if (req.getRealName() != null) {
            user.setRealName(req.getRealName());
        }
        if (req.getPhone() != null) {
            user.setPhone(StringUtils.hasText(req.getPhone()) ? req.getPhone() : null);
        }
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        userMapper.updateById(user);
        return toProfileResponse(user, loginUser.getActiveRole());
    }

    public EnterpriseResponse getEnterprise() {
        Long userId = SecurityUtils.getUserId();
        SupplierProfile profile = supplierProfileMapper.selectOne(
                new LambdaQueryWrapper<SupplierProfile>().eq(SupplierProfile::getUserId, userId));
        if (profile == null) {
            return null;
        }
        return toEnterpriseResponse(profile);
    }

    @Transactional(rollbackFor = Exception.class)
    public EnterpriseResponse saveEnterprise(EnterpriseUpdateRequest req) {
        Long userId = SecurityUtils.getUserId();
        SupplierProfile profile = supplierProfileMapper.selectOne(
                new LambdaQueryWrapper<SupplierProfile>().eq(SupplierProfile::getUserId, userId));

        if (profile == null) {
            if (existsByCreditCode(req.getCreditCode(), null)) {
                throw new BusinessException(ResultCode.ACCOUNT_EXISTS, "该统一社会信用代码已被使用");
            }
            profile = new SupplierProfile();
            profile.setUserId(userId);
            profile.setStatus(0);
            fillEnterprise(profile, req);
            supplierProfileMapper.insert(profile);
        } else {
            if (!req.getCreditCode().equals(profile.getCreditCode())
                    && existsByCreditCode(req.getCreditCode(), profile.getId())) {
                throw new BusinessException(ResultCode.ACCOUNT_EXISTS, "该统一社会信用代码已被使用");
            }
            if (profile.getStatus() != null && profile.getStatus() == 1) {
                profile.setContactName(req.getContactName());
                profile.setContactPhone(req.getContactPhone());
                profile.setBankName(req.getBankName());
                profile.setBankAccount(req.getBankAccount());
            } else {
                fillEnterprise(profile, req);
                profile.setStatus(0);
            }
            supplierProfileMapper.updateById(profile);
        }
        return toEnterpriseResponse(profile);
    }

    private void fillEnterprise(SupplierProfile profile, EnterpriseUpdateRequest req) {
        profile.setCompanyName(req.getCompanyName());
        profile.setCreditCode(req.getCreditCode());
        profile.setLegalPerson(req.getLegalPerson());
        profile.setAddress(req.getAddress());
        profile.setContactName(req.getContactName());
        profile.setContactPhone(req.getContactPhone());
        profile.setBankName(req.getBankName());
        profile.setBankAccount(req.getBankAccount());
        profile.setLicenseFileId(req.getLicenseFileId());
    }

    private ProfileResponse toProfileResponse(SysUser user, String activeRole) {
        List<String> roles = listApprovedRoleCodes(user.getId());
        return ProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(roles)
                .activeRole(activeRole)
                .build();
    }

    private EnterpriseResponse toEnterpriseResponse(SupplierProfile profile) {
        return EnterpriseResponse.builder()
                .id(profile.getId())
                .companyName(profile.getCompanyName())
                .creditCode(profile.getCreditCode())
                .legalPerson(profile.getLegalPerson())
                .address(profile.getAddress())
                .contactName(profile.getContactName())
                .contactPhone(profile.getContactPhone())
                .bankName(profile.getBankName())
                .bankAccount(profile.getBankAccount())
                .licenseFileId(profile.getLicenseFileId())
                .status(profile.getStatus())
                .build();
    }

    private List<String> listApprovedRoleCodes(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getAuditStatus, 1));
        if (userRoles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds))
                .stream().map(SysRole::getCode).collect(Collectors.toList());
    }

    private SysUser requireUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    private boolean existsByPhone(String phone, Long excludeUserId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone);
        if (excludeUserId != null) {
            wrapper.ne(SysUser::getId, excludeUserId);
        }
        return userMapper.selectCount(wrapper) > 0;
    }

    private boolean existsByCreditCode(String creditCode, Long excludeId) {
        LambdaQueryWrapper<SupplierProfile> wrapper = new LambdaQueryWrapper<SupplierProfile>()
                .eq(SupplierProfile::getCreditCode, creditCode);
        if (excludeId != null) {
            wrapper.ne(SupplierProfile::getId, excludeId);
        }
        return supplierProfileMapper.selectCount(wrapper) > 0;
    }
}
