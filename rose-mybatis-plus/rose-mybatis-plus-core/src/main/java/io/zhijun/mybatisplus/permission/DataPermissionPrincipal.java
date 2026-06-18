package io.zhijun.mybatisplus.permission;

import org.springframework.lang.Nullable;

/**
 * Opaque principal passed to data permission resolver.
 */
public final class DataPermissionPrincipal {

    private final boolean bypass;

    @Nullable
    private final Object context;

    private DataPermissionPrincipal(boolean bypass, @Nullable Object context) {
        this.bypass = bypass;
        this.context = context;
    }

    public static DataPermissionPrincipal bypass() {
        return new DataPermissionPrincipal(true, null);
    }

    public static DataPermissionPrincipal of(@Nullable Object context) {
        return new DataPermissionPrincipal(false, context);
    }

    public boolean isBypass() {
        return bypass;
    }

    @Nullable
    public Object getContext() {
        return context;
    }
}
