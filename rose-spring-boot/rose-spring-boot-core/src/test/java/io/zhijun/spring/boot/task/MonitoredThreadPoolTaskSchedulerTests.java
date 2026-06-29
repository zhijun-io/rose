package io.zhijun.spring.boot.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ScheduledExecutorService;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

class MonitoredThreadPoolTaskSchedulerTests {

    @Test
    void shouldMonitorExecutorWhenMeterRegistryPresent() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(SimpleMeterRegistry.class, SimpleMeterRegistry::new);
        context.refresh();

        MonitoredThreadPoolTaskScheduler scheduler = createScheduler(context, "monitoredTaskScheduler");

        ScheduledExecutorService delegate = scheduler.getScheduledExecutor();

        assertThat(delegate).isSameAs(scheduler.getScheduledExecutor());

        scheduler.destroy();
        context.close();
    }

    @Test
    void shouldFallbackToNativeExecutorWhenMeterRegistryMissing() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();

        MonitoredThreadPoolTaskScheduler scheduler = createScheduler(context, "plainTaskScheduler");

        ScheduledExecutorService delegate = scheduler.getScheduledExecutor();

        assertThat(delegate).isSameAs(scheduler.getScheduledExecutor());

        scheduler.destroy();
        context.close();
    }

    private static MonitoredThreadPoolTaskScheduler createScheduler(GenericApplicationContext context, String beanName) {
        MonitoredThreadPoolTaskScheduler scheduler = new MonitoredThreadPoolTaskScheduler();
        scheduler.setBeanName(beanName);
        scheduler.setPoolSize(1);
        scheduler.setApplicationContext(context);
        scheduler.initialize();
        scheduler.afterSingletonsInstantiated();
        return scheduler;
    }
}
