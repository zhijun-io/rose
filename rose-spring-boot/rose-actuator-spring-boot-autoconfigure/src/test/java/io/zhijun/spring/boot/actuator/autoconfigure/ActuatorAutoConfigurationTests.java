package io.zhijun.spring.boot.actuator.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import io.zhijun.spring.boot.task.MonitoredThreadPoolTaskScheduler;

class ActuatorAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(ActuatorAutoConfiguration.class));

    @Test
    void shouldNotRegisterActuatorTaskSchedulerWithoutMeterRegistry() {
        contextRunner.run(context -> assertThat(context)
                .doesNotHaveBean(ActuatorAutoConfiguration.ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME));
    }

    @Test
    void shouldRegisterActuatorTaskSchedulerWhenMeterRegistryPresent() {
        contextRunner.withUserConfiguration(MeterRegistryConfiguration.class).run(context -> {
            assertThat(context).hasBean(ActuatorAutoConfiguration.ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME);
            assertThat(context.getBean(ActuatorAutoConfiguration.ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME))
                    .isInstanceOf(MonitoredThreadPoolTaskScheduler.class);
        });
    }

    @Test
    void shouldApplyTaskSchedulerProperties() {
        contextRunner
                .withUserConfiguration(MeterRegistryConfiguration.class)
                .withPropertyValues(
                        "rose.actuator.task-scheduler.pool-size=3",
                        "rose.actuator.task-scheduler.thread-name-prefix=custom-actuator-")
                .run(context -> {
                    assertThat(context.getEnvironment().getProperty("rose.actuator.task-scheduler.pool-size"))
                            .isEqualTo("3");
                    MonitoredThreadPoolTaskScheduler scheduler = context.getBean(
                            ActuatorAutoConfiguration.ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME,
                            MonitoredThreadPoolTaskScheduler.class);
                    assertThat(scheduler.getThreadNamePrefix()).isEqualTo("custom-actuator-");
                    assertThat(scheduler.isDaemon()).isTrue();
                });
    }

    @Test
    void shouldNotReplaceApplicationTaskScheduler() {
        contextRunner
                .withUserConfiguration(MeterRegistryConfiguration.class, ApplicationTaskSchedulerConfiguration.class)
                .run(context -> {
                    assertThat(context).hasBean("applicationTaskScheduler");
                    assertThat(context).hasBean(ActuatorAutoConfiguration.ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME);
                    assertThat(context.getBean("applicationTaskScheduler", TaskScheduler.class))
                            .isNotSameAs(context.getBean(
                                    ActuatorAutoConfiguration.ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME));
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
    static class ApplicationTaskSchedulerConfiguration {

        @Bean
        TaskScheduler applicationTaskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(1);
            scheduler.initialize();
            return scheduler;
        }
    }
}
