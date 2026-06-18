package io.zhijun.boot.actuate.autoconfigure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import io.zhijun.boot.actuate.MonitoredThreadPoolTaskScheduler;

import static io.zhijun.boot.actuate.PropertyConstants.TASK_SCHEDULER_PROPERTY_NAME_PREFIX;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

/**
 * Actuator Spring Boot auto-configuration.
 */
@AutoConfigureOrder(LOWEST_PRECEDENCE)
public class ActuatorAutoConfiguration {

    static final String DEFAULT_TASK_SCHEDULER_POOL_SIZE = "1";

    static final String TASK_SCHEDULER_POOL_SIZE_PROPERTY_NAME = TASK_SCHEDULER_PROPERTY_NAME_PREFIX + "pool-size";

    static final String TASK_SCHEDULER_POOL_SIZE_VALUE_EXPRESSION = "${"
            + TASK_SCHEDULER_POOL_SIZE_PROPERTY_NAME + ":" + DEFAULT_TASK_SCHEDULER_POOL_SIZE + "}";

    static final String DEFAULT_TASK_SCHEDULER_THREAD_NAME_PREFIX = "rose-spring-boot-actuator-task-";

    static final String TASK_SCHEDULER_THREAD_NAME_PREFIX_PROPERTY_NAME =
            TASK_SCHEDULER_PROPERTY_NAME_PREFIX + "thread-name-prefix";

    static final String TASK_SCHEDULER_THREAD_NAME_PREFIX_VALUE_EXPRESSION = "${"
            + TASK_SCHEDULER_THREAD_NAME_PREFIX_PROPERTY_NAME + ":" + DEFAULT_TASK_SCHEDULER_THREAD_NAME_PREFIX + "}";

    /**
     * Bean name of the actuator {@link ThreadPoolTaskScheduler}.
     */
    public static final String ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME = "actuatorTaskScheduler";

    static final String METER_REGISTRY_CLASS_NAME = "io.micrometer.core.instrument.MeterRegistry";

    @ConditionalOnBean(type = METER_REGISTRY_CLASS_NAME)
    @Bean(name = ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME, destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler actuatorTaskScheduler(
            @Value(TASK_SCHEDULER_POOL_SIZE_VALUE_EXPRESSION) int poolSize,
            @Value(TASK_SCHEDULER_THREAD_NAME_PREFIX_VALUE_EXPRESSION) String threadNamePrefix) {
        MonitoredThreadPoolTaskScheduler threadPoolTaskScheduler = new MonitoredThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(poolSize);
        threadPoolTaskScheduler.setDaemon(true);
        threadPoolTaskScheduler.setThreadNamePrefix(threadNamePrefix);
        return threadPoolTaskScheduler;
    }

}
