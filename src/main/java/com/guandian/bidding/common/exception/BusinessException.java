package com.guandian.bidding.common.exception;

import com.guandian.bidding.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常：由 Service 层主动抛出，统一异常处理器转为响应体。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String msg) {
        super(msg);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}
