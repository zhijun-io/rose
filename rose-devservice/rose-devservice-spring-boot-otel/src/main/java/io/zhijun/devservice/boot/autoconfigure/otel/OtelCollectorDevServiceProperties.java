package io.zhijun.devservice.boot.autoconfigure.otel;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * OpenTelemetry Collector dev service properties.
 */
@ConfigurationProperties(prefix = OtelCollectorDevServiceProperties.CONFIG_PREFIX)
public class OtelCollectorDevServiceProperties extends BaseDevServiceProperties {

    public static final String SERVICE_NAME = "otel-collector";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "otel/opentelemetry-collector-contrib:0.96.0";

    /** Fixed host port for collector OTLP gRPC; 0 selects a random port. */
    private int otlpGrpcPort = RANDOM_PORT;

    public OtelCollectorDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setShared(true);
    }

    public int getOtlpGrpcPort() {
        return otlpGrpcPort;
    }

    public void setOtlpGrpcPort(int otlpGrpcPort) {
        this.otlpGrpcPort = otlpGrpcPort;
    }
}
