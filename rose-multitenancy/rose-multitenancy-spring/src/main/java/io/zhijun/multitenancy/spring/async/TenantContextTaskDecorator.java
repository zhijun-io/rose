package io.zhijun.multitenancy.spring.async;

import org.springframework.core.task.TaskDecorator;

import io.zhijun.multitenancy.core.context.TenantContext;

/**
 * Propagates the current {@link TenantContext} to asynchronous task execution.
 */
public final class TenantContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return runnable;
        }
        return new TenantPropagatingRunnable(tenantId, runnable);
    }

    private static final class TenantPropagatingRunnable implements Runnable {

        private final String tenantId;

        private final Runnable delegate;

        private TenantPropagatingRunnable(String tenantId, Runnable delegate) {
            this.tenantId = tenantId;
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try (TenantContext.Scope ignored = TenantContext.bind(tenantId)) {
                delegate.run();
            }
        }
    }

}
