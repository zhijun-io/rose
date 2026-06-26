package io.zhijun.devservice.boot.autoconfigure.openlit;

import org.junit.jupiter.api.Test;

import io.zhijun.devservice.test.BaseDevServicesContainerTests;
import io.zhijun.devservice.test.DockerTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link OpenLitContainer}.
 */
class OpenLitContainerIT extends BaseDevServicesContainerTests<OpenLitContainer> {

    static {
        DockerTestSupport.configureIfNeeded();
    }

    @Test
    void whenExposedPortsAreNotConfigured() {
        OpenLitContainer container = new OpenLitContainer(new OpenLitDevServiceProperties());
        container.configure();
        assertNoPortBindingsConfigured(container.getPortBindings());
    }

    @Test
    void whenExposedPortsAreConfigured() {
        OpenLitDevServiceProperties properties = new OpenLitDevServiceProperties();
        properties.setPort(1234);
        properties.setOtlpGrpcPort(5678);
        properties.setOtlpHttpPort(9067);

        OpenLitContainer container = new OpenLitContainer(properties);
        container.configure();
        assertPortBindingsConfigured(container.getPortBindings(), portBindings -> assertThat(portBindings)
                .anyMatch(binding -> binding.startsWith(properties.getPort() + ":" + OpenLitContainer.UI_PORT))
                .anyMatch(binding -> binding.startsWith(properties.getOtlpGrpcPort() + ":" + OpenLitContainer.OTLP_GRPC_PORT))
                .anyMatch(binding -> binding.startsWith(properties.getOtlpHttpPort() + ":" + OpenLitContainer.OTLP_HTTP_PORT)));
    }

}
