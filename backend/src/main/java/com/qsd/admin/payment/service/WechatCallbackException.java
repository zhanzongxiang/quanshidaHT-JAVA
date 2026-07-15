package com.qsd.admin.payment.service;

public class WechatCallbackException extends RuntimeException {
    private final boolean retryable;
    private final String category;

    public WechatCallbackException(String category, String message, boolean retryable) {
        super(message);
        this.category = category;
        this.retryable = retryable;
    }

    public WechatCallbackException(String category, String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.category = category;
        this.retryable = retryable;
    }

    public boolean retryable() {
        return retryable;
    }

    public String category() {
        return category;
    }
}
