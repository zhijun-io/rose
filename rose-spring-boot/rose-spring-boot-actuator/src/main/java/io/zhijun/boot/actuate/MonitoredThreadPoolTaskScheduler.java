package io.zhijun.boot.actuate;

import java.util.concurrent.ScheduledExecutorService;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

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
        if (context == null || beanName == null) {
            return;
        }
        MeterRegistry registry = context.getBean(MeterRegistry.class);
        this.delegate = ExecutorServiceMetrics.monitor(registry, super.getScheduledExecutor(), beanName);
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
