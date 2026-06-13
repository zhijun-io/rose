package io.zhijun.dev.services.postgresql;

import java.lang.reflect.Field;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.PostgreSQLContainer;

import static io.zhijun.dev.services.postgresql.RosePostgreSqlContainer.READY_REGEX;
import static io.zhijun.dev.services.postgresql.RosePostgreSqlContainer.SKIPPING_INITIALIZATION_REGEX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RosePostgreSqlContainer}.
 */
class RosePostgreSqlContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RosePostgreSqlContainer container = new RosePostgreSqlContainer(new PostgresqlDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenExposedPortsAreConfigured() {
        PostgresqlDevServicesProperties properties = new PostgresqlDevServicesProperties();
        properties.setPort(1234);

        RosePostgreSqlContainer container = new RosePostgreSqlContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + PostgreSQLContainer.POSTGRESQL_PORT));
    }

    @Test
    void withCustomWaitStrategy() {
        PostgresqlDevServicesProperties properties = new PostgresqlDevServicesProperties();
        RosePostgreSqlContainer container = new RosePostgreSqlContainer(properties);
        LogMessageWaitStrategy waitStrategy = getWaitStrategy(container);

        Duration actualTimeout = getStartupTimeout(waitStrategy);
        assertThat(actualTimeout).isEqualTo(properties.getStartupTimeout());

        String regex = getWaitStrategyRegex(waitStrategy);
        assertThat(regex).isEqualTo("(" + READY_REGEX + "|" + SKIPPING_INITIALIZATION_REGEX + ")");

        int times = getWaitStrategyTimes(waitStrategy);
        assertThat(times).isEqualTo(2);
    }

    /**
     * Helper method to extract the WaitStrategy from a GenericContainer using reflection.
     */
    private LogMessageWaitStrategy getWaitStrategy(GenericContainer<?> container) {
        Field waitStrategyField = ReflectionUtils.findField(GenericContainer.class, "waitStrategy");
        assertThat(waitStrategyField).isNotNull();
        ReflectionUtils.makeAccessible(waitStrategyField);
        return (LogMessageWaitStrategy) ReflectionUtils.getField(waitStrategyField, container);
    }

    /**
     * Helper method to extract the startup timeout from a WaitStrategy using reflection.
     */
    private Duration getStartupTimeout(LogMessageWaitStrategy waitStrategy) {
        Field startupTimeoutField = ReflectionUtils.findField(waitStrategy.getClass(), "startupTimeout");
        assertThat(startupTimeoutField).isNotNull();
        ReflectionUtils.makeAccessible(startupTimeoutField);
        return (Duration) ReflectionUtils.getField(startupTimeoutField, waitStrategy);
    }

    /**
     * Helper method to extract the regex from a WaitStrategy using reflection.
     */
    private String getWaitStrategyRegex(LogMessageWaitStrategy waitStrategy) {
        Field regexField = ReflectionUtils.findField(waitStrategy.getClass(), "regEx");
        assertThat(regexField).isNotNull();
        ReflectionUtils.makeAccessible(regexField);
        return (String) ReflectionUtils.getField(regexField, waitStrategy);
    }

    /**
     * Helper method to extract the times from a WaitStrategy using reflection.
     */
    private Integer getWaitStrategyTimes(LogMessageWaitStrategy waitStrategy) {
        Field timesField = ReflectionUtils.findField(waitStrategy.getClass(), "times");
        assertThat(timesField).isNotNull();
        ReflectionUtils.makeAccessible(timesField);
        return (Integer) ReflectionUtils.getField(timesField, waitStrategy);
    }

}
