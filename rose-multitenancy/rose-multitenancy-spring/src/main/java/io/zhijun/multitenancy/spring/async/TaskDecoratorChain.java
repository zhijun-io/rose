package io.zhijun.multitenancy.spring.async;

import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.Nullable;

/**
 * Composes {@link TaskDecorator} instances without relying on executor-specific APIs.
 */
public final class TaskDecoratorChain {

    private TaskDecoratorChain() {}

    /**
     * Applies {@code outer} before {@code inner} when the decorated task runs.
     */
    public static TaskDecorator chain(TaskDecorator outer, TaskDecorator inner) {
        return runnable -> outer.decorate(inner.decorate(runnable));
    }

    /**
     * Returns {@code tenantDecorator} when no existing decorator is configured.
     */
    public static TaskDecorator merge(@Nullable TaskDecorator existing, TaskDecorator tenantDecorator) {
        if (existing == null || existing == tenantDecorator) {
            return tenantDecorator;
        }
        if (existing instanceof TenantContextTaskDecorator) {
            return existing;
        }
        return chain(tenantDecorator, existing);
    }
}
