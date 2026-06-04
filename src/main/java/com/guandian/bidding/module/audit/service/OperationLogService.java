package com.guandian.bidding.module.audit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.util.RequestIpUtils;
import com.guandian.bidding.module.audit.dto.OperationLogResponse;
import com.guandian.bidding.module.audit.entity.OperationLog;
import com.guandian.bidding.module.audit.enums.OperationAction;
import com.guandian.bidding.module.audit.enums.OperationModule;
import com.guandian.bidding.module.audit.mapper.OperationLogMapper;
import com.guandian.bidding.module.manager.support.ManagerProjectGuard;
import com.guandian.bidding.security.LoginUser;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final ManagerProjectGuard projectGuard;

    public void record(OperationModule module, OperationAction action, Long bizId, String detail) {
        OperationLog log = new OperationLog();
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null) {
            log.setUserId(loginUser.getUserId());
            log.setRoleCode(loginUser.getActiveRole());
        }
        log.setModule(module.name());
        log.setAction(action.name());
        log.setBizId(bizId);
        log.setDetail(truncate(detail, 1000));
        log.setIp(RequestIpUtils.clientIp());
        operationLogMapper.insert(log);
    }

    public void recordProject(OperationModule module, OperationAction action, Long projectId, String detail) {
        record(module, action, projectId, withProject(projectId, detail));
    }

    public PageResult<OperationLogResponse> listForAdmin(String module, String action, Long bizId, Long userId,
                                                         long pageNum, long pageSize) {
        LambdaQueryWrapper<OperationLog> wrapper = buildQuery(module, action, bizId, userId, null);
        return queryPage(wrapper, pageNum, pageSize);
    }

    public PageResult<OperationLogResponse> listByProject(Long projectId, long pageNum, long pageSize) {
        projectGuard.requireOwnedProject(projectId);
        LambdaQueryWrapper<OperationLog> wrapper = buildQuery(null, null, null, null, projectId);
        return queryPage(wrapper, pageNum, pageSize);
    }

    public static String withProject(Long projectId, String detail) {
        String prefix = "projectId=" + projectId;
        if (!StringUtils.hasText(detail)) {
            return prefix;
        }
        if (detail.contains("projectId=")) {
            return detail;
        }
        return prefix + ", " + detail;
    }

    private LambdaQueryWrapper<OperationLog> buildQuery(String module, String action, Long bizId, Long userId,
                                                        Long projectId) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .eq(StringUtils.hasText(module), OperationLog::getModule, module)
                .eq(StringUtils.hasText(action), OperationLog::getAction, action)
                .eq(bizId != null, OperationLog::getBizId, bizId)
                .eq(userId != null, OperationLog::getUserId, userId)
                .orderByDesc(OperationLog::getCreateTime);
        if (projectId != null) {
            String projectToken = "projectId=" + projectId;
            wrapper.and(w -> w.eq(OperationLog::getBizId, projectId)
                    .or().like(OperationLog::getDetail, projectToken));
        }
        return wrapper;
    }

    private PageResult<OperationLogResponse> queryPage(LambdaQueryWrapper<OperationLog> wrapper,
                                                       long pageNum, long pageSize) {
        Page<OperationLog> page = operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<OperationLogResponse> list = page.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    private OperationLogResponse toResponse(OperationLog log) {
        return OperationLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .roleCode(log.getRoleCode())
                .module(log.getModule())
                .action(log.getAction())
                .bizId(log.getBizId())
                .detail(log.getDetail())
                .ip(log.getIp())
                .createTime(log.getCreateTime())
                .build();
    }

    private String truncate(String value, int maxLen) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
}
