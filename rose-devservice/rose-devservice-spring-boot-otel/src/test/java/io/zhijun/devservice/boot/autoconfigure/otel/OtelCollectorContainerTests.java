package io.zhijun.devservice.boot.autoconfigure.otel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

/**
 * Unit test for {@link OtelCollectorContainer}.
 */
class OtelCollectorContainerTests extends BaseDevServicesContainerTests<OtelCollectorContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        OtelCollectorContainer container = new OtelCollectorContainer(new OtelCollectorDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        OtelCollectorDevServiceProperties properties = new OtelCollectorDevServiceProperties();
        properties.setPort(1234);
        properties.setOtlpGrpcPort(5678);

        OtelCollectorContainer container = new OtelCollectorContainer(properties);
        container.configure();
        assertPortBindingsConfigured(
                container.getPortBindings(),
                portBindings -> assertThat(portBindings)
                        .anyMatch(binding ->
                                binding.startsWith(properties.getPort() + ":" + OtelCollectorContainer.OTLP_HTTP_PORT))
                        .anyMatch(binding -> binding.startsWith(
                                properties.getOtlpGrpcPort() + ":" + OtelCollectorContainer.OTLP_GRPC_PORT)));
    }
}
