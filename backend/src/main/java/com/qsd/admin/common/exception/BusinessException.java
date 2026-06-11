package com.qsd.admin.common.exception;

public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        this(ErrorCode.BUSINESS_RULE_FAILED, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
