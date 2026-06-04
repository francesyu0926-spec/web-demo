package com.guandian.bidding.module.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.notify.dto.NotificationResponse;
import com.guandian.bidding.module.notify.dto.UnreadCountResponse;
import com.guandian.bidding.module.notify.entity.Notification;
import com.guandian.bidding.module.notify.enums.NotificationType;
import com.guandian.bidding.module.notify.mapper.NotificationMapper;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public PageResult<NotificationResponse> list(Boolean isRead, long pageNum, long pageSize) {
        Long userId = SecurityUtils.getUserId();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(isRead != null, Notification::getIsRead, Boolean.TRUE.equals(isRead) ? 1 : 0)
                .orderByDesc(Notification::getCreateTime);

        Page<Notification> page = notificationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<NotificationResponse> list = page.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public NotificationResponse markRead(Long id) {
        Notification notification = requireOwned(id);
        if (notification.getIsRead() == null || notification.getIsRead() != 1) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
        return toResponse(notification);
    }

    public UnreadCountResponse unreadCount() {
        Long userId = SecurityUtils.getUserId();
        long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        return new UnreadCountResponse(count);
    }

    public void send(Long userId, NotificationType type, String title, String content, Long bizId) {
        if (userId == null || type == null || !StringUtils.hasText(title)) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type.name());
        notification.setTitle(truncate(title, 128));
        notification.setContent(truncate(content, 500));
        notification.setBizId(bizId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    public void sendBatch(Collection<Long> userIds, NotificationType type, String title, String content, Long bizId) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        userIds.stream().distinct().forEach(userId -> send(userId, type, title, content, bizId));
    }

    private Notification requireOwned(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null || !SecurityUtils.getUserId().equals(notification.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return notification;
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .bizId(notification.getBizId())
                .isRead(notification.getIsRead() != null && notification.getIsRead() == 1)
                .createTime(notification.getCreateTime())
                .build();
    }

    private String truncate(String value, int maxLen) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
}
