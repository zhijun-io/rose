package io.zhijun.multitenancy.spring.async;

import org.springframework.core.task.TaskDecorator;
import org.jspecify.annotations.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 * Applies {@link TenantContextTaskDecorator} to Spring executors that expose
 * {@code setTaskDecorator}.
 */
public final class TaskExecutorDecoratorSupport {

    private TaskExecutorDecoratorSupport() {}

    public static boolean supportsTaskDecoration(Object executor) {
        return ReflectionUtils.findMethod(executor.getClass(), "setTaskDecorator", TaskDecorator.class) != null;
    }

    public static void applyTenantDecorator(Object executor, TenantContextTaskDecorator tenantDecorator) {
        if (!supportsTaskDecoration(executor)) {
            return;
        }
        TaskDecorator merged = TaskDecoratorChain.merge(resolveTaskDecorator(executor), tenantDecorator);
        setTaskDecorator(executor, merged);
    }

    @Nullable
    static TaskDecorator resolveTaskDecorator(Object executor) {
        java.lang.reflect.Method getter = ReflectionUtils.findMethod(executor.getClass(), "getTaskDecorator");
        if (getter != null) {
            return (TaskDecorator) ReflectionUtils.invokeMethod(getter, executor);
        }
        return readTaskDecoratorField(executor);
    }

    private static void setTaskDecorator(Object executor, TaskDecorator decorator) {
        java.lang.reflect.Method setter =
                ReflectionUtils.findMethod(executor.getClass(), "setTaskDecorator", TaskDecorator.class);
        if (setter == null) {
            throw new IllegalStateException("Executor does not expose setTaskDecorator: "
                    + executor.getClass().getName());
        }
        ReflectionUtils.invokeMethod(setter, executor, decorator);
    }

    @Nullable
    private static TaskDecorator readTaskDecoratorField(Object executor) {
        java.lang.reflect.Field field = ReflectionUtils.findField(executor.getClass(), "taskDecorator");
        if (field == null) {
            return null;
        }
        ReflectionUtils.makeAccessible(field);
        return (TaskDecorator) ReflectionUtils.getField(field, executor);
    }
}
