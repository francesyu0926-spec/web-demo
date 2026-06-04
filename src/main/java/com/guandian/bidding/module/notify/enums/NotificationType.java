package com.guandian.bidding.module.notify.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    AUDIT("审核通知"),
    INVITE("邀请通知"),
    OPEN("开标通知"),
    NEGOTIATION("谈判/报价通知"),
    REPORT("评标报告"),
    AWARD("定标通知"),
    SYSTEM("系统通知");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }
}
