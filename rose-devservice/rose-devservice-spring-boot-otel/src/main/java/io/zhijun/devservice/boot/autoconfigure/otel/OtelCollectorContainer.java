package io.zhijun.devservice.boot.autoconfigure.otel;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.devservice.core.container.ContainerConfigurer;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.util.OtlpPorts;

/**
 * OpenTelemetry Collector container configured for Rose DevService.
 */
final class OtelCollectorContainer extends GenericContainer<OtelCollectorContainer> {

    static final String COMPATIBLE_IMAGE_NAME =
            DockerImageName.parse(OtelCollectorDevServiceProperties.DEFAULT_IMAGE_NAME).getUnversionedPart();

    static final int OTLP_GRPC_PORT = OtlpPorts.GRPC;

    static final int OTLP_HTTP_PORT = OtlpPorts.HTTP;

    private final OtelCollectorDevServiceProperties properties;

    OtelCollectorContainer(OtelCollectorDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        addExposedPorts(OTLP_GRPC_PORT, OTLP_HTTP_PORT);
        waitingFor(Wait.forListeningPort());

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();
        if (BaseDevServiceProperties.isFixedPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), OTLP_HTTP_PORT);
        }
        if (BaseDevServiceProperties.isFixedPort(properties.getOtlpGrpcPort())) {
            addFixedExposedPort(properties.getOtlpGrpcPort(), OTLP_GRPC_PORT);
        }
    }

    Integer getGrpcPort() {
        return getMappedPort(OTLP_GRPC_PORT);
    }

    Integer getHttpPort() {
        return getMappedPort(OTLP_HTTP_PORT);
    }

    String getOtlpHttpUrl() {
        return "http://" + getHost() + ":" + getHttpPort();
    }

    String getOtlpGrpcUrl() {
        return "http://" + getHost() + ":" + getGrpcPort();
    }
}
