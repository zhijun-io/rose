package io.zhijun.devservice.boot.autoconfigure.openlit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * OpenLit dev service properties.
 */
@ConfigurationProperties(prefix = OpenLitDevServiceProperties.CONFIG_PREFIX)
public class OpenLitDevServiceProperties extends BaseDevServiceProperties {

    public static final String SERVICE_NAME = "openlit";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "ghcr.io/openlit/openlit:1.21.1";

    public static final String DEFAULT_CLICKHOUSE_IMAGE_NAME = "clickhouse/clickhouse-server:24.8";

    /** Dynamic property published when the dev container is running. */
    public static final String UI_URL_PROPERTY = CONFIG_PREFIX + ".ui-url";

    /**
     * ClickHouse sidecar image used by the OpenLit stack.
     * Non-distroless image: honors CLICKHOUSE_USER/PASSWORD for Testcontainers.
     */
    private String clickhouseImageName = DEFAULT_CLICKHOUSE_IMAGE_NAME;

    /** Fixed host port for OpenLit OTLP gRPC; 0 selects a random port. */
    private int otlpGrpcPort = RANDOM_PORT;

    /** Fixed host port for OpenLit OTLP HTTP; 0 selects a random port. */
    private int otlpHttpPort = RANDOM_PORT;

    public OpenLitDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setShared(true);
        setStartupTimeout(HEAVY_STARTUP_TIMEOUT);
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
