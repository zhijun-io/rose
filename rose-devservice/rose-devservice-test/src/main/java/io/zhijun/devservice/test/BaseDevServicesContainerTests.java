package io.zhijun.devservice.test;

import java.util.List;
import java.util.function.Consumer;

import org.testcontainers.containers.GenericContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base test class for container configuration unit test.
 *
 * @param <T> the specific {@link GenericContainer} implementation type
 */
public abstract class BaseDevServicesContainerTests<T extends GenericContainer<?>> {

    protected void assertNoPortBindingsConfigured(List<String> portBindings) {
        assertThat(portBindings).isEmpty();
    }

    protected void assertPortBindingsConfigured(List<String> portBindings, Consumer<List<String>> assertions) {
        assertThat(portBindings).isNotNull();
        assertions.accept(portBindings);
    }

}
