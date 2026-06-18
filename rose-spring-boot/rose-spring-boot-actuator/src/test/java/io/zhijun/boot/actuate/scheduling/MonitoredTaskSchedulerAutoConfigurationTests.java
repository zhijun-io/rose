package io.zhijun.boot.actuate.scheduling;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoredTaskSchedulerAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MonitoredTaskSchedulerAutoConfiguration.class))
            .withUserConfiguration(MeterRegistryConfiguration.class);

    @Test
    void shouldRegisterMonitoredTaskSchedulerWhenMeterRegistryPresent() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TaskScheduler.class);
            assertThat(context.getBean(TaskScheduler.class)).isInstanceOf(MonitoredThreadPoolTaskScheduler.class);
        });
    }

    @Test
    void shouldNotOverrideExistingTaskScheduler() {
        contextRunner.withUserConfiguration(CustomTaskSchedulerConfiguration.class).run(context -> {
            assertThat(context).hasSingleBean(TaskScheduler.class);
            assertThat(context.getBean(TaskScheduler.class)).isSameAs(CustomTaskSchedulerConfiguration.scheduler);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class MeterRegistryConfiguration {

        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomTaskSchedulerConfiguration {

        static final MonitoredThreadPoolTaskScheduler scheduler = new MonitoredThreadPoolTaskScheduler();

        @Bean
        TaskScheduler taskScheduler() {
            scheduler.initialize();
            return scheduler;
        }
    }
}
