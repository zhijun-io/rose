package io.zhijun.spring.core;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ScheduledExecutorService;

/**
 * {@link ThreadPoolTaskScheduler} with Micrometer executor metrics.
 */
public class MonitoredThreadPoolTaskScheduler extends ThreadPoolTaskScheduler
        implements ApplicationContextAware, SmartInitializingSingleton {

    private String beanName;

    private ApplicationContext context;

    private volatile ScheduledExecutorService delegate;

    @Override
    public ScheduledExecutorService getScheduledExecutor() throws IllegalStateException {
        ScheduledExecutorService executor = this.delegate;
        if (executor != null) {
            return executor;
        }
        return super.getScheduledExecutor();
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (this.context == null || this.beanName == null) {
            return;
        }
        try {
            MeterRegistry registry = this.context.getBean(MeterRegistry.class);
            this.delegate = ExecutorServiceMetrics.monitor(registry, super.getScheduledExecutor(), this.beanName);
        } catch (NoSuchBeanDefinitionException ex) {
            this.delegate = super.getScheduledExecutor();
        }
    }

    @Override
    public void setBeanName(String name) {
        super.setBeanName(name);
        this.beanName = name;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
