package io.zhijun.dev.services.openlit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RoseOpenLitContainer}.
 */
class RoseOpenLitContainerTests {

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseOpenLitContainer container = new RoseOpenLitContainer(new OpenLitDevServicesProperties());
        container.configure();
        assertThat(container.getPortBindings()).isEmpty();
    }

    @Test
    void whenExposedPortsAreConfigured() {
        OpenLitDevServicesProperties properties = new OpenLitDevServicesProperties();
        properties.setPort(1234);
        properties.setOtlpGrpcPort(5678);
        properties.setOtlpHttpPort(9067);

        RoseOpenLitContainer container = new RoseOpenLitContainer(properties);
        container.configure();

        java.util.List<String> portBindings = container.getPortBindings();
        assertThat(portBindings).isNotNull();
        assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(properties.getPort() + ":" + RoseOpenLitContainer.UI_PORT))
                .anyMatch(binding -> binding.startsWith(properties.getOtlpGrpcPort() + ":" + RoseOpenLitContainer.OTLP_GRPC_PORT))
                .anyMatch(binding -> binding.startsWith(properties.getOtlpHttpPort() + ":" + RoseOpenLitContainer.OTLP_HTTP_PORT));
    }

}
