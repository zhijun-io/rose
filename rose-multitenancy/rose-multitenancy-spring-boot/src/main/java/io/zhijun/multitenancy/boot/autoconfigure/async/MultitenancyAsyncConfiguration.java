package io.zhijun.multitenancy.boot.autoconfigure.async;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

import io.zhijun.multitenancy.spring.async.TenantContextTaskDecorator;

/**
 * Auto-configuration for propagating multitenancy context to asynchronous executors.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = MultitenancyAsyncProperties.CONFIG_PREFIX, name = "propagation-enabled",
        havingValue = "true", matchIfMissing = true)
public final class MultitenancyAsyncConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "tenantContextTaskDecorator")
    TenantContextTaskDecorator tenantContextTaskDecorator() {
        return new TenantContextTaskDecorator();
    }

    @Bean
    static BeanPostProcessor tenantContextTaskExecutorBeanPostProcessor(TenantContextTaskDecorator taskDecorator) {
        return new TenantContextTaskExecutorBeanPostProcessor(taskDecorator);
    }

    private static final class TenantContextTaskExecutorBeanPostProcessor implements BeanPostProcessor {

        private final TenantContextTaskDecorator taskDecorator;

        private TenantContextTaskExecutorBeanPostProcessor(TenantContextTaskDecorator taskDecorator) {
            this.taskDecorator = taskDecorator;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof ThreadPoolTaskExecutor) {
                ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) bean;
                TaskDecorator existing = getTaskDecorator(executor);
                if (existing == null) {
                    executor.setTaskDecorator(taskDecorator);
                } else if (existing != taskDecorator) {
                    executor.setTaskDecorator(runnable -> taskDecorator.decorate(existing.decorate(runnable)));
                }
            }
            return bean;
        }

        /**
         * Reads the {@code taskDecorator} field via reflection because Spring Framework 5.3
         * does not expose a public getter.
         */
        @Nullable
        private static TaskDecorator getTaskDecorator(ThreadPoolTaskExecutor executor) {
            Field field = ReflectionUtils.findField(ThreadPoolTaskExecutor.class, "taskDecorator");
            if (field == null) {
                return null;
            }
            ReflectionUtils.makeAccessible(field);
            return (TaskDecorator) ReflectionUtils.getField(field, executor);
        }

    }

}
