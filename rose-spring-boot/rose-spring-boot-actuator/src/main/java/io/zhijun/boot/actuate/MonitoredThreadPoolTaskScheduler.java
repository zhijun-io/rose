package io.zhijun.boot.actuate;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import io.zhijun.core.concurrent.DelegatingScheduledExecutorService;

/**
 * {@link ThreadPoolTaskScheduler} with Micrometer executor metrics.
 */
public class MonitoredThreadPoolTaskScheduler extends ThreadPoolTaskScheduler
        implements ApplicationContextAware, SmartInitializingSingleton {

    private String beanName;

    private ApplicationContext context;

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
        return delegate;
    }

    @Override
    public void afterSingletonsInstantiated() {
        MeterRegistry registry = context.getBean(MeterRegistry.class);
        ScheduledExecutorService scheduledExecutor = super.getScheduledExecutor();
        this.delegate.setDelegate(ExecutorServiceMetrics.monitor(registry, scheduledExecutor, beanName));
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
