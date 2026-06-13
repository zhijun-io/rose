package io.zhijun.multitenancy.core.context;

import java.util.concurrent.Callable;

import org.springframework.lang.Nullable;

import io.zhijun.core.support.Incubating;
import io.zhijun.multitenancy.core.exceptions.TenantNotFoundException;

/**
 * Thread-local tenant context for the current execution scope.
 */
@Incubating
public final class TenantContext {

    private static final ThreadLocal<String> TENANT_IDENTIFIER = new ThreadLocal<String>();

    private TenantContext() {}

    public static Carrier where(String tenantIdentifier) {
        return new Carrier(tenantIdentifier);
    }

    @Nullable
    public static String getTenantIdentifier() {
        return TENANT_IDENTIFIER.get();
    }

    public static String getRequiredTenantIdentifier() {
        String tenantIdentifier = TENANT_IDENTIFIER.get();
        if (tenantIdentifier == null) {
            throw new TenantNotFoundException("No tenant found in the current context");
        }
        return tenantIdentifier;
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
                restore(previous);
            }
        }

        public <T> T call(Callable<T> action) throws Exception {
            String previous = TENANT_IDENTIFIER.get();
            TENANT_IDENTIFIER.set(tenantIdentifier);
            try {
                return action.call();
            } finally {
                restore(previous);
            }
        }

        private static void restore(String previous) {
            if (previous != null) {
                TENANT_IDENTIFIER.set(previous);
            } else {
                TENANT_IDENTIFIER.remove();
            }
        }
    }

}
