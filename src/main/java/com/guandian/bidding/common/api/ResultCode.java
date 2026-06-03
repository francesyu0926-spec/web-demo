package com.guandian.bidding.common.api;

import lombok.Getter;

/**
 * 业务错误码（与《02-接口设计》错误码表对应）。
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "success"),

    UNAUTHORIZED(1001, "未登录或登录已失效"),
    FORBIDDEN(1003, "无权限"),

    TENDER_STATUS_INVALID(2001, "项目状态不允许该操作"),
    REGISTRATION_CLOSED(2002, "报名或投标已截止"),
    REGISTRATION_DUPLICATE(2003, "请勿重复报名"),
    BID_FILE_LOCKED(2004, "开标前或未解密不可下载投标文件"),
    REGISTRATION_UNPAID(2005, "报名尚未缴费"),

    EXPERT_NOT_SIGNED(3001, "专家未签到，不可评分"),
    REPORT_NOT_ALL_SIGNED(3002, "评标报告未全部签名，不可导出"),

    PARAM_ERROR(4000, "参数错误"),
    SYSTEM_ERROR(5000, "系统异常");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
