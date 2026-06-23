package io.zhijun.devservice.opentelemetry.collector;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link RoseOtelCollectorContainer}.
 */
class RoseOtelCollectorContainerTests extends BaseDevServicesContainerTests<RoseOtelCollectorContainer> {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseOtelCollectorContainer container = new RoseOtelCollectorContainer(new OtelCollectorDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        OtelCollectorDevServiceProperties properties = new OtelCollectorDevServiceProperties();
        properties.setPort(1234);
        properties.setOtlpGrpcPort(5678);

        RoseOtelCollectorContainer container = new RoseOtelCollectorContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseOtelCollectorContainer.OTLP_HTTP_PORT))
                .anyMatch(binding -> binding.startsWith(
                        properties.getOtlpGrpcPort() + ":" + RoseOtelCollectorContainer.OTLP_GRPC_PORT)));
    }

}
