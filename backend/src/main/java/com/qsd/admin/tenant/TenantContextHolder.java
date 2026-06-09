package com.qsd.admin.tenant;

import com.qsd.admin.common.exception.BusinessException;

public final class TenantContextHolder {
    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(TenantContext tenantContext) {
        CONTEXT.set(tenantContext);
    }

    public static TenantContext get() {
        return CONTEXT.get();
    }

    public static Long requireTenantId() {
        TenantContext context = CONTEXT.get();
        if (context == null || context.tenantId() == null) {
            throw new BusinessException("tenant context is missing");
        }
        return context.tenantId();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
