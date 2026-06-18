package io.zhijun.boot.actuate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import io.zhijun.boot.env.defaults.DefaultConfigPropertiesEnvironmentPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies {@code config/default/actuator.properties} loads with Rose Spring Boot core.
 */
class ActuatorDefaultConfigPropertiesTests {

    @Test
    void shouldLoadActuatorDefaultProperties() {
        MockEnvironment environment = new MockEnvironment();
        new DefaultConfigPropertiesEnvironmentPostProcessor()
                .postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty(PropertyConstants.TASK_SCHEDULER_PROPERTY_NAME_PREFIX + "pool-size"))
                .isEqualTo("1");
        assertThat(environment.getProperty(PropertyConstants.TASK_SCHEDULER_PROPERTY_NAME_PREFIX + "thread-name-prefix"))
                .isEqualTo("rose-spring-boot-actuator-task-");
    }

}
