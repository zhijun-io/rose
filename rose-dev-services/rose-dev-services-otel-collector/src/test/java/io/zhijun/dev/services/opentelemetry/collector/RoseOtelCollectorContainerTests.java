package io.zhijun.dev.services.opentelemetry.collector;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseOtelCollectorContainer}.
 */
class RoseOtelCollectorContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseOtelCollectorContainer container = new RoseOtelCollectorContainer(new OtelCollectorDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenExposedPortsAreConfigured() {
        OtelCollectorDevServicesProperties properties = new OtelCollectorDevServicesProperties();
        properties.setPort(1234);
        properties.setOtlpGrpcPort(5678);

        RoseOtelCollectorContainer container = new RoseOtelCollectorContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(
                        properties.getPort() + ":" + RoseOtelCollectorContainer.OTLP_HTTP_PORT))
                .anyMatch(binding -> binding.startsWith(
                        properties.getOtlpGrpcPort() + ":" + RoseOtelCollectorContainer.OTLP_GRPC_PORT));
    }

}
