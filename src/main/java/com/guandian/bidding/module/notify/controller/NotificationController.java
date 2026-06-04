package com.guandian.bidding.module.notify.controller;

import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.R;
import com.guandian.bidding.module.notify.dto.NotificationResponse;
import com.guandian.bidding.module.notify.dto.UnreadCountResponse;
import com.guandian.bidding.module.notify.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "消息通知", description = "我的消息")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "我的消息列表")
    @GetMapping
    public R<PageResult<NotificationResponse>> list(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(notificationService.list(isRead, pageNum, pageSize));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public R<NotificationResponse> markRead(@PathVariable Long id) {
        return R.ok(notificationService.markRead(id));
    }

    @Operation(summary = "未读消息数")
    @GetMapping("/unread-count")
    public R<UnreadCountResponse> unreadCount() {
        return R.ok(notificationService.unreadCount());
    }
}
