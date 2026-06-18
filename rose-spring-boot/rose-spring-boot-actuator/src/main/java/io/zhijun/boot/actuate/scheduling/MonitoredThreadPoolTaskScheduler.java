package io.zhijun.boot.actuate.scheduling;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

/**
 * {@link ThreadPoolTaskScheduler} with Micrometer executor metrics.
 */
public final class MonitoredThreadPoolTaskScheduler extends ThreadPoolTaskScheduler
        implements ApplicationContextAware, SmartInitializingSingleton {

    private String beanName;

    private ApplicationContext applicationContext;

    private DelegatingScheduledExecutorService delegate;

    @Override
    protected ScheduledExecutorService createExecutor(int poolSize, ThreadFactory threadFactory,
            RejectedExecutionHandler rejectedExecutionHandler) {
        ScheduledExecutorService scheduledExecutor = super.createExecutor(poolSize, threadFactory, rejectedExecutionHandler);
        this.delegate = new DelegatingScheduledExecutorService(scheduledExecutor);
        return scheduledExecutor;
    }

    @Override
    public ScheduledExecutorService getScheduledExecutor() throws IllegalStateException {
        if (delegate == null) {
            return super.getScheduledExecutor();
        }
        return delegate;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (delegate == null || applicationContext == null) {
            return;
        }
        MeterRegistry registry = applicationContext.getBean(MeterRegistry.class);
        ScheduledExecutorService scheduledExecutor = super.getScheduledExecutor();
        delegate.setDelegate(ExecutorServiceMetrics.monitor(registry, scheduledExecutor, beanName));
    }

    @Override
    public void setBeanName(String name) {
        super.setBeanName(name);
        this.beanName = name;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
