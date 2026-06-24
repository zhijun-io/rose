package io.zhijun.mybatisplus.core.permission;

/**
 * Opaque principal passed to data permission resolver.
 */
public final class DataPermissionPrincipal {

    private final boolean bypass;

    private final Object context;

    private DataPermissionPrincipal(boolean bypass, Object context) {
        this.bypass = bypass;
        this.context = context;
    }

    public static DataPermissionPrincipal bypass() {
        return new DataPermissionPrincipal(true, null);
    }

    public static DataPermissionPrincipal of(Object context) {
        return new DataPermissionPrincipal(false, context);
    }

    public boolean isBypass() {
        return bypass;
    }

    public Object getContext() {
        return context;
    }
}
