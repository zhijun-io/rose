/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zhijun.spring.boot.actuator.autoconfigure;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ActuatorAutoConfiguration} 自动装配单元测试。
 * <p>
 * 验证该自动配置等价于 microsphere-spring-boot-actuator 的
 * {@code io.microsphere.spring.boot.actuate.autoconfigure.ActuatorAutoConfiguration}。
 */
class ActuatorAutoConfigurationTests {

    private static final String AUTO_CONFIGURATION_NAME =
            ActuatorAutoConfiguration.class.getName();

    // ====== 文件注册验证 ======

    @Test
    void shouldRegisterAutoConfigurationInImportsFile() throws IOException {
        assertThat(resource("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"))
                .contains(AUTO_CONFIGURATION_NAME);
    }


    // ====== TaskScheduler Bean 注册验证 ======

    @Test
    void shouldRegisterActuatorTaskSchedulerWhenMeterRegistryPresent() {
        new ApplicationContextRunner()
                .withBean(MeterRegistry.class, () -> new SimpleMeterRegistry())
                .withConfiguration(AutoConfigurations.of(
                        ActuatorAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(ThreadPoolTaskScheduler.class);
                    assertThat(context.getBean(
                            ActuatorAutoConfiguration.ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME,
                            ThreadPoolTaskScheduler.class)).isNotNull();
                });
    }

    @Test
    void shouldBackOffActuatorTaskSchedulerWhenMeterRegistryAbsent() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        ActuatorAutoConfiguration.class))
                .run(context ->
                        assertThat(context).doesNotHaveBean(
                                ActuatorAutoConfiguration.ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME));
    }

    @Test
    void shouldUseConfiguredProperties() {
        new ApplicationContextRunner()
                .withBean(MeterRegistry.class, () -> new SimpleMeterRegistry())
                .withConfiguration(AutoConfigurations.of(
                        ActuatorAutoConfiguration.class))
                .withPropertyValues(
                        "rose.actuator.task-scheduler.pool-size=5",
                        "rose.actuator.task-scheduler.thread-name-prefix=test-task-")
                .run(context -> {
                    ThreadPoolTaskScheduler scheduler = context.getBean(
                            ActuatorAutoConfiguration.ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME,
                            ThreadPoolTaskScheduler.class);
                    // threadNamePrefix is set via @Value before bean initialization
                    assertThat(scheduler.getThreadNamePrefix())
                            .isEqualTo("test-task-");
                    // After afterPropertiesSet(), underlying executor should have corePoolSize >= 5
                    assertThat(scheduler.getScheduledExecutor()).isNotNull();
                    // getPoolSize() returns actual thread count (0 before any task submitted),
                    // so check the core pool size from the executor directly
                    if (scheduler.getScheduledExecutor() instanceof ScheduledThreadPoolExecutor) {
                        assertThat(((ScheduledThreadPoolExecutor) scheduler.getScheduledExecutor()).getCorePoolSize())
                                .isEqualTo(5);
                    }
                });
    }

    // ====== 工具方法 ======

    private String resource(String name) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
        assertThat(inputStream).as(name).isNotNull();
        try {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } finally {
            inputStream.close();
        }
    }
}
