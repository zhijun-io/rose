package io.zhijun.devservice.boot.autoconfigure.otel;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * OpenTelemetry Collector dev service properties.
 */
@ConfigurationProperties(prefix = OtelCollectorDevServiceProperties.CONFIG_PREFIX)
public class OtelCollectorDevServiceProperties extends BaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.otel-collector";

    /** Fixed host port for collector OTLP gRPC; 0 selects a random port. */
    private int otlpGrpcPort = 0;

    public OtelCollectorDevServiceProperties() {
        setImageName("otel/opentelemetry-collector-contrib:0.96.0");
        setShared(true);
    }

    public int getOtlpGrpcPort() {
        return otlpGrpcPort;
    }

    public void setOtlpGrpcPort(int otlpGrpcPort) {
        this.otlpGrpcPort = otlpGrpcPort;
    }
}
