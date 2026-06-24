package io.zhijun.devservice.boot.autoconfigure.openlit;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;
import io.zhijun.devservice.test.DockerTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link RoseOpenLitContainer}.
 */
class RoseOpenLitContainerIT extends BaseDevServicesContainerTests<RoseOpenLitContainer> {

    static {
        DockerTestSupport.configureIfNeeded();
    }

    @Test
    void whenExposedPortsAreNotConfigured() {
        RoseOpenLitContainer container = new RoseOpenLitContainer(new OpenLitDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        OpenLitDevServiceProperties properties = new OpenLitDevServiceProperties();
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
