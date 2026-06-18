package io.zhijun.boot.actuate.scheduling;

import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Auto-configuration for Micrometer-backed {@link TaskScheduler}.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(MetricsAutoConfiguration.class)
@ConditionalOnClass({ ThreadPoolTaskScheduler.class, MeterRegistry.class })
@ConditionalOnBean(MeterRegistry.class)
@ConditionalOnProperty(prefix = MonitoredSchedulingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(MonitoredSchedulingProperties.class)
public final class MonitoredTaskSchedulerAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(TaskScheduler.class)
    MonitoredThreadPoolTaskScheduler roseTaskScheduler(MonitoredSchedulingProperties properties) {
        MonitoredThreadPoolTaskScheduler scheduler = new MonitoredThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getPoolSize());
        scheduler.setThreadNamePrefix(properties.getThreadNamePrefix());
        scheduler.initialize();
        return scheduler;
    }
}
