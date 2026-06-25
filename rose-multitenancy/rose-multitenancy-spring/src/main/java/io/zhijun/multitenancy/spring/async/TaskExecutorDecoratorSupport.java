package io.zhijun.multitenancy.spring.async;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.Nullable;

/**
 * Applies {@link TenantContextTaskDecorator} to Spring executors that expose
 * {@code setTaskDecorator} without using Spring Framework reflection utilities.
 */
public final class TaskExecutorDecoratorSupport {

    private TaskExecutorDecoratorSupport() {
    }

    public static boolean supportsTaskDecoration(Object executor) {
        return findMethod(executor.getClass(), "setTaskDecorator", TaskDecorator.class) != null;
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
        Method getter = findMethod(executor.getClass(), "getTaskDecorator");
        if (getter != null) {
            try {
                return (TaskDecorator) getter.invoke(executor);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Failed to read TaskDecorator from " + executor.getClass().getName(), ex);
            }
        }
        return readTaskDecoratorField(executor);
    }

    private static void setTaskDecorator(Object executor, TaskDecorator decorator) {
        Method setter = findMethod(executor.getClass(), "setTaskDecorator", TaskDecorator.class);
        if (setter == null) {
            throw new IllegalStateException("Executor does not expose setTaskDecorator: " + executor.getClass().getName());
        }
        try {
            setter.invoke(executor, decorator);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to set TaskDecorator on " + executor.getClass().getName(), ex);
        }
    }

    @Nullable
    private static TaskDecorator readTaskDecoratorField(Object executor) {
        Class<?> type = executor.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("taskDecorator");
                field.setAccessible(true);
                return (TaskDecorator) field.get(executor);
            } catch (NoSuchFieldException ex) {
                type = type.getSuperclass();
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Failed to read taskDecorator field from " + executor.getClass().getName(), ex);
            }
        }
        return null;
    }

    @Nullable
    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getMethod(name, parameterTypes);
            } catch (NoSuchMethodException ex) {
                try {
                    Method method = current.getDeclaredMethod(name, parameterTypes);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ignored) {
                    current = current.getSuperclass();
                }
            }
        }
        return null;
    }

}
