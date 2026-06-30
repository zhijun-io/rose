package io.zhijun.multitenancy.core.context;

import java.util.concurrent.Callable;


import org.jspecify.annotations.Nullable;

import io.zhijun.multitenancy.core.exception.TenantNotFoundException;

/**
 * Thread-local multitenancy context for the current execution scope.
 */

public final class TenantContext {

    private static final ThreadLocal<String> TENANT_IDENTIFIER = new ThreadLocal<String>();

    private TenantContext() {}

    public static Carrier where(String tenantIdentifier) {
        return new Carrier(tenantIdentifier);
    }

    @Nullable
    public static String getTenantId() {
        return TENANT_IDENTIFIER.get();
    }

    public static String getRequiredTenantId() {
        String tenantIdentifier = TENANT_IDENTIFIER.get();
        if (tenantIdentifier == null) {
            throw new TenantNotFoundException("No tenant found in the current context");
        }
        return tenantIdentifier;
    }

    /**
     * Binds the multitenancy for the current thread and returns a scope that restores the previous value on close.
     */
    public static Scope bind(String tenantIdentifier) {
        if (tenantIdentifier == null || tenantIdentifier.trim().isEmpty()) {
            throw new IllegalArgumentException("tenantIdentifier cannot be null or empty");
        }
        String previous = TENANT_IDENTIFIER.get();
        TENANT_IDENTIFIER.set(tenantIdentifier);
        return new Scope(previous);
    }

    public static final class Scope implements AutoCloseable {

        private final String previousTenantIdentifier;

        private Scope(String previousTenantIdentifier) {
            this.previousTenantIdentifier = previousTenantIdentifier;
        }

        @Override
        public void close() {
            TenantContext.restore(previousTenantIdentifier);
        }
    }

    public static final class Carrier {

        private final String tenantIdentifier;

        private Carrier(String tenantIdentifier) {
            this.tenantIdentifier = tenantIdentifier;
        }

        public void run(Runnable action) {
            String previous = TENANT_IDENTIFIER.get();
            TENANT_IDENTIFIER.set(tenantIdentifier);
            try {
                action.run();
            } finally {
                TenantContext.restore(previous);
            }
        }

        public <T> T call(Callable<T> action) throws Exception {
            String previous = TENANT_IDENTIFIER.get();
            TENANT_IDENTIFIER.set(tenantIdentifier);
            try {
                return action.call();
            } finally {
                TenantContext.restore(previous);
            }
        }
    }

    private static void restore(@Nullable String previous) {
        if (previous != null) {
            TENANT_IDENTIFIER.set(previous);
        } else {
            TENANT_IDENTIFIER.remove();
        }
    }
}
