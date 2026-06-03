package com.guandian.bidding.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.auth.dto.RoleApplicationAuditRequest;
import com.guandian.bidding.module.auth.dto.RoleApplicationRequest;
import com.guandian.bidding.module.auth.dto.RoleApplicationResponse;
import com.guandian.bidding.module.auth.entity.RoleApplication;
import com.guandian.bidding.module.auth.entity.SysRole;
import com.guandian.bidding.module.auth.entity.SysUser;
import com.guandian.bidding.module.auth.entity.SysUserRole;
import com.guandian.bidding.module.auth.mapper.RoleApplicationMapper;
import com.guandian.bidding.module.auth.mapper.SysRoleMapper;
import com.guandian.bidding.module.auth.mapper.SysUserMapper;
import com.guandian.bidding.module.auth.mapper.SysUserRoleMapper;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleApplicationService {

    private static final Set<String> ALLOWED_ROLES = Set.of("MANAGER", "EXPERT");

    private final RoleApplicationMapper applicationMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Transactional(rollbackFor = Exception.class)
    public RoleApplicationResponse apply(RoleApplicationRequest req) {
        Long userId = SecurityUtils.getUserId();
        String applyRole = req.getApplyRole().toUpperCase();
        if (!ALLOWED_ROLES.contains(applyRole)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅支持申请 MANAGER 或 EXPERT");
        }
        if ("EXPERT".equals(applyRole) && !StringUtils.hasText(req.getMajor())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "申请专家时请填写专业领域");
        }
        if (hasApprovedRole(userId, applyRole)) {
            throw new BusinessException(ResultCode.ALREADY_HAS_ROLE);
        }
        if (hasPendingApplication(userId, applyRole)) {
            throw new BusinessException(ResultCode.APPLICATION_PENDING);
        }

        RoleApplication application = new RoleApplication();
        application.setUserId(userId);
        application.setApplyRole(applyRole);
        application.setMajor(req.getMajor());
        application.setAttachId(req.getAttachId());
        application.setAuditStatus(0);
        applicationMapper.insert(application);

        SysUser user = userMapper.selectById(userId);
        return toResponse(application, user);
    }

    public PageResult<RoleApplicationResponse> listForAdmin(Integer auditStatus, long pageNum, long pageSize) {
        LambdaQueryWrapper<RoleApplication> wrapper = new LambdaQueryWrapper<RoleApplication>()
                .orderByDesc(RoleApplication::getCreateTime);
        if (auditStatus != null) {
            wrapper.eq(RoleApplication::getAuditStatus, auditStatus);
        }

        Page<RoleApplication> page = applicationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Long> userIds = page.getRecords().stream()
                .map(RoleApplication::getUserId).distinct().collect(Collectors.toList());
        Map<Long, SysUser> userMap = userIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        List<RoleApplicationResponse> list = page.getRecords().stream()
                .map(app -> toResponse(app, userMap.get(app.getUserId())))
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public RoleApplicationResponse audit(Long id, RoleApplicationAuditRequest req) {
        if (req.getAuditStatus() == null || (req.getAuditStatus() != 1 && req.getAuditStatus() != 2)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "auditStatus 仅支持 1(通过) 或 2(驳回)");
        }

        RoleApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(ResultCode.APPLICATION_NOT_FOUND);
        }
        if (application.getAuditStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该申请已审核，不可重复操作");
        }

        application.setAuditStatus(req.getAuditStatus());
        application.setAuditRemark(req.getRemark());
        application.setAuditBy(SecurityUtils.getUserId());
        application.setAuditTime(LocalDateTime.now());
        applicationMapper.updateById(application);

        if (req.getAuditStatus() == 1) {
            grantRole(application.getUserId(), application.getApplyRole());
        }

        SysUser user = userMapper.selectById(application.getUserId());
        return toResponse(application, user);
    }

    private void grantRole(Long userId, String roleCode) {
        SysRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode));
        if (role == null) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "角色未配置: " + roleCode);
        }
        SysUserRole existing = userRoleMapper.selectOne(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getRoleId, role.getId()));
        if (existing == null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRole.setAuditStatus(1);
            userRoleMapper.insert(userRole);
        } else if (existing.getAuditStatus() != 1) {
            existing.setAuditStatus(1);
            userRoleMapper.updateById(existing);
        }
    }

    private boolean hasApprovedRole(Long userId, String roleCode) {
        SysRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, roleCode));
        if (role == null) {
            return false;
        }
        return userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getRoleId, role.getId())
                        .eq(SysUserRole::getAuditStatus, 1)) > 0;
    }

    private boolean hasPendingApplication(Long userId, String applyRole) {
        return applicationMapper.selectCount(
                new LambdaQueryWrapper<RoleApplication>()
                        .eq(RoleApplication::getUserId, userId)
                        .eq(RoleApplication::getApplyRole, applyRole)
                        .eq(RoleApplication::getAuditStatus, 0)) > 0;
    }

    private RoleApplicationResponse toResponse(RoleApplication app, SysUser user) {
        return RoleApplicationResponse.builder()
                .id(app.getId())
                .userId(app.getUserId())
                .username(user != null ? user.getUsername() : null)
                .realName(user != null ? user.getRealName() : null)
                .applyRole(app.getApplyRole())
                .major(app.getMajor())
                .attachId(app.getAttachId())
                .auditStatus(app.getAuditStatus())
                .auditRemark(app.getAuditRemark())
                .auditTime(app.getAuditTime())
                .createTime(app.getCreateTime())
                .build();
    }
}
