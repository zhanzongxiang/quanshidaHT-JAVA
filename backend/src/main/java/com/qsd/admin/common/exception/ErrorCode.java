package com.qsd.admin.common.exception;

public final class ErrorCode {
    public static final int BUSINESS_RULE_FAILED = 40000;
    public static final int VALIDATION_FAILED = 40001;
    public static final int INVALID_ARGUMENT = 40002;
    public static final int TENANT_CONTEXT_REQUIRED = 40010;
    public static final int AUTHENTICATION_REQUIRED = 40100;
    public static final int AUTHENTICATION_FAILED = 40101;
    public static final int SESSION_INVALID = 40102;
    public static final int AUTHORIZATION_DENIED = 40300;
    public static final int RESOURCE_NOT_FOUND = 40400;
    public static final int RESOURCE_CONFLICT = 40900;
    public static final int STATE_INVALID = 40901;
    public static final int RATE_LIMITED = 42900;
    public static final int INTERNAL_ERROR = 50000;

    private ErrorCode() {
    }

    public static boolean isAuthError(int code) {
        return code >= 40100 && code < 40200;
    }
}
