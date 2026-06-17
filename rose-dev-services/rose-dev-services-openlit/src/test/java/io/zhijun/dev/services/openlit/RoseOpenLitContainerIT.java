package io.zhijun.dev.services.openlit;

import org.junit.jupiter.api.Test;

import io.zhijun.dev.services.tests.BaseDevServicesContainerTests;
import io.zhijun.dev.services.tests.DockerTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RoseOpenLitContainer}.
 */
class RoseOpenLitContainerIT extends BaseDevServicesContainerTests<RoseOpenLitContainer> {

    static {
        DockerTestSupport.configureIfNeeded();
    }

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseOpenLitContainer container = new RoseOpenLitContainer(new OpenLitDevServicesProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        OpenLitDevServicesProperties properties = new OpenLitDevServicesProperties();
        properties.setPort(1234);
        properties.setOtlpGrpcPort(5678);
        properties.setOtlpHttpPort(9067);

        RoseOpenLitContainer container = new RoseOpenLitContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(properties.getPort() + ":" + RoseOpenLitContainer.UI_PORT))
                .anyMatch(binding -> binding.startsWith(properties.getOtlpGrpcPort() + ":" + RoseOpenLitContainer.OTLP_GRPC_PORT))
                .anyMatch(binding -> binding.startsWith(properties.getOtlpHttpPort() + ":" + RoseOpenLitContainer.OTLP_HTTP_PORT)));
    }

}
