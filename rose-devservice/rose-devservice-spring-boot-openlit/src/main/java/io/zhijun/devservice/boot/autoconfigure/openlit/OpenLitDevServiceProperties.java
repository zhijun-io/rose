package io.zhijun.devservice.boot.autoconfigure.openlit;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * OpenLit dev service properties.
 */
@ConfigurationProperties(prefix = OpenLitDevServiceProperties.CONFIG_PREFIX)
public class OpenLitDevServiceProperties extends BaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.openlit";

    /**
     * ClickHouse sidecar image used by the OpenLit stack.
     * Non-distroless image: honors CLICKHOUSE_USER/PASSWORD for Testcontainers.
     */
    private String clickhouseImageName = "clickhouse/clickhouse-server:24.8";

    /** Fixed host port for OpenLit OTLP gRPC; 0 selects a random port. */
    private int otlpGrpcPort = 0;

    /** Fixed host port for OpenLit OTLP HTTP; 0 selects a random port. */
    private int otlpHttpPort = 0;

    public OpenLitDevServiceProperties() {
        setImageName("ghcr.io/openlit/openlit:1.21.1");
        setShared(true);
        setStartupTimeout(Duration.ofMinutes(2));
    }

    public String getClickhouseImageName() {
        return clickhouseImageName;
    }

    public void setClickhouseImageName(String clickhouseImageName) {
        this.clickhouseImageName = clickhouseImageName;
    }

    public int getOtlpGrpcPort() {
        return otlpGrpcPort;
    }

    public void setOtlpGrpcPort(int otlpGrpcPort) {
        this.otlpGrpcPort = otlpGrpcPort;
    }

    public int getOtlpHttpPort() {
        return otlpHttpPort;
    }

    public void setOtlpHttpPort(int otlpHttpPort) {
        this.otlpHttpPort = otlpHttpPort;
    }
}
