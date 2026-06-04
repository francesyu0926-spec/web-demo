package com.guandian.bidding.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.auth.dto.TendererInviteCreateRequest;
import com.guandian.bidding.module.auth.dto.TendererInviteRespondRequest;
import com.guandian.bidding.module.auth.dto.TendererInviteResponse;
import com.guandian.bidding.module.auth.entity.SysRole;
import com.guandian.bidding.module.auth.entity.SysUser;
import com.guandian.bidding.module.auth.entity.SysUserRole;
import com.guandian.bidding.module.auth.entity.TendererInvite;
import com.guandian.bidding.module.auth.mapper.SysRoleMapper;
import com.guandian.bidding.module.auth.mapper.SysUserMapper;
import com.guandian.bidding.module.auth.mapper.SysUserRoleMapper;
import com.guandian.bidding.module.auth.mapper.TendererInviteMapper;
import com.guandian.bidding.module.audit.enums.OperationAction;
import com.guandian.bidding.module.audit.enums.OperationModule;
import com.guandian.bidding.module.audit.service.OperationLogService;
import com.guandian.bidding.module.notify.enums.NotificationType;
import com.guandian.bidding.module.notify.service.NotificationService;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TendererInviteService {

    private static final String TENDERER_ROLE = "TENDERER";

    private final TendererInviteMapper inviteMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final NotificationService notificationService;
    private final OperationLogService operationLogService;

    @Transactional(rollbackFor = Exception.class)
    public TendererInviteResponse create(TendererInviteCreateRequest req) {
        requireManager();
        Long inviterId = SecurityUtils.getUserId();
        String phone = normalizePhone(req.getInviteePhone());

        if (hasPendingInvite(inviterId, phone)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该手机号已有待处理的邀请");
        }

        SysUser invitee = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhone, phone));

        TendererInvite invite = new TendererInvite();
        invite.setInviterId(inviterId);
        invite.setInviteeName(req.getInviteeName().trim());
        invite.setInviteePhone(phone);
        invite.setInviteeUserId(invitee != null ? invitee.getId() : null);
        invite.setStatus(0);
        invite.setCreateBy(inviterId);
        inviteMapper.insert(invite);

        if (invitee != null) {
            SysUser inviter = userMapper.selectById(inviterId);
            String inviterName = inviter != null && StringUtils.hasText(inviter.getRealName())
                    ? inviter.getRealName() : "项目经理";
            notificationService.send(invitee.getId(), NotificationType.INVITE,
                    "招标人邀请",
                    inviterName + " 邀请您成为招标人，请及时处理。",
                    invite.getId());
        }

        operationLogService.record(OperationModule.AUTH, OperationAction.TENDERER_INVITE, invite.getId(),
                "inviteePhone=" + phone + ", inviteeName=" + invite.getInviteeName());

        return toResponse(invite, userMapper.selectById(inviterId));
    }

    public PageResult<TendererInviteResponse> listReceived(Integer status, long pageNum, long pageSize) {
        SysUser currentUser = requireCurrentUser();
        LambdaQueryWrapper<TendererInvite> wrapper = new LambdaQueryWrapper<TendererInvite>()
                .and(w -> {
                    w.eq(TendererInvite::getInviteeUserId, currentUser.getId());
                    if (StringUtils.hasText(currentUser.getPhone())) {
                        w.or().eq(TendererInvite::getInviteePhone, currentUser.getPhone());
                    }
                })
                .eq(status != null, TendererInvite::getStatus, status)
                .orderByDesc(TendererInvite::getCreateTime);

        Page<TendererInvite> page = inviteMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<Long, SysUser> inviterMap = loadInviters(page.getRecords());
        List<TendererInviteResponse> list = page.getRecords().stream()
                .map(invite -> toResponse(invite, inviterMap.get(invite.getInviterId())))
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public TendererInviteResponse respond(Long id, TendererInviteRespondRequest req) {
        SysUser currentUser = requireCurrentUser();
        TendererInvite invite = inviteMapper.selectById(id);
        if (invite == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!isInvitee(invite, currentUser)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权处理该邀请");
        }
        if (invite.getStatus() != null && invite.getStatus() != 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该邀请已处理，不可重复操作");
        }

        if (Boolean.TRUE.equals(req.getAccept())) {
            if (invite.getInviteeUserId() == null) {
                invite.setInviteeUserId(currentUser.getId());
            }
            grantTendererRole(currentUser.getId());
            invite.setStatus(1);
            notificationService.send(invite.getInviterId(), NotificationType.INVITE,
                    "招标人邀请已接受",
                    currentUserDisplayName(currentUser) + " 已接受您的招标人邀请。",
                    invite.getId());
            operationLogService.record(OperationModule.AUTH, OperationAction.TENDERER_ACCEPT, invite.getId(),
                    "inviteeUserId=" + currentUser.getId());
        } else {
            invite.setStatus(2);
            notificationService.send(invite.getInviterId(), NotificationType.INVITE,
                    "招标人邀请已拒绝",
                    currentUserDisplayName(currentUser) + " 已拒绝您的招标人邀请。",
                    invite.getId());
            operationLogService.record(OperationModule.AUTH, OperationAction.TENDERER_REJECT, invite.getId(),
                    "inviteeUserId=" + currentUser.getId());
        }
        invite.setUpdateBy(currentUser.getId());
        inviteMapper.updateById(invite);

        return toResponse(invite, userMapper.selectById(invite.getInviterId()));
    }

    private void requireManager() {
        if (!"MANAGER".equals(SecurityUtils.requireLoginUser().getActiveRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "请切换为项目经理身份");
        }
    }

    private SysUser requireCurrentUser() {
        return userMapper.selectById(SecurityUtils.getUserId());
    }

    private boolean hasPendingInvite(Long inviterId, String phone) {
        return inviteMapper.selectCount(new LambdaQueryWrapper<TendererInvite>()
                .eq(TendererInvite::getInviterId, inviterId)
                .eq(TendererInvite::getInviteePhone, phone)
                .eq(TendererInvite::getStatus, 0)) > 0;
    }

    private boolean isInvitee(TendererInvite invite, SysUser user) {
        if (user.getId().equals(invite.getInviteeUserId())) {
            return true;
        }
        return StringUtils.hasText(user.getPhone())
                && user.getPhone().equals(invite.getInviteePhone());
    }

    private void grantTendererRole(Long userId) {
        SysRole role = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, TENDERER_ROLE));
        if (role == null) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "招标人角色未配置");
        }
        SysUserRole existing = userRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
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

    private Map<Long, SysUser> loadInviters(List<TendererInvite> invites) {
        List<Long> inviterIds = invites.stream()
                .map(TendererInvite::getInviterId)
                .distinct()
                .collect(Collectors.toList());
        if (inviterIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(inviterIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
    }

    private TendererInviteResponse toResponse(TendererInvite invite, SysUser inviter) {
        return TendererInviteResponse.builder()
                .id(invite.getId())
                .inviterId(invite.getInviterId())
                .inviterName(inviter != null ? displayName(inviter) : null)
                .inviteeUserId(invite.getInviteeUserId())
                .inviteeName(invite.getInviteeName())
                .inviteePhone(invite.getInviteePhone())
                .status(invite.getStatus())
                .createTime(invite.getCreateTime())
                .updateTime(invite.getUpdateTime())
                .build();
    }

    private String currentUserDisplayName(SysUser user) {
        return displayName(user);
    }

    private String displayName(SysUser user) {
        if (StringUtils.hasText(user.getRealName())) {
            return user.getRealName();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return user.getPhone();
    }

    private String normalizePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "手机号不能为空");
        }
        return phone.trim();
    }
}
