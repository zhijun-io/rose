package io.zhijun.devservice.boot.autoconfigure.otel;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.zhijun.devservice.core.container.ContainerConfigurer;
import io.zhijun.devservice.core.util.ContainerUtils;

/**
 * OpenTelemetry Collector container configured for Rose DevService.
 */
final class RoseOtelCollectorContainer extends GenericContainer<RoseOtelCollectorContainer> {

    static final String COMPATIBLE_IMAGE_NAME = "otel/opentelemetry-collector-contrib";

    static final int OTLP_GRPC_PORT = 4317;

    static final int OTLP_HTTP_PORT = 4318;

    private final OtelCollectorDevServiceProperties properties;

    RoseOtelCollectorContainer(OtelCollectorDevServiceProperties properties) {
        super(DockerImageName.parse(properties.getImageName()).asCompatibleSubstituteFor(COMPATIBLE_IMAGE_NAME));
        this.properties = properties;

        addExposedPorts(OTLP_GRPC_PORT, OTLP_HTTP_PORT);
        waitingFor(Wait.forListeningPort());

        ContainerConfigurer.base(this, properties);
    }

    @Override
    protected void configure() {
        super.configure();
        if (ContainerUtils.isValidPort(properties.getPort())) {
            addFixedExposedPort(properties.getPort(), OTLP_HTTP_PORT);
        }
        if (ContainerUtils.isValidPort(properties.getOtlpGrpcPort())) {
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
