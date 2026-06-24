package io.zhijun.devservice.boot.autoconfigure.openlit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.AbstractBaseDevServiceProperties;

/**
 * OpenLit dev service properties.
 */
@ConfigurationProperties(prefix = OpenLitDevServiceProperties.CONFIG_PREFIX)
public class OpenLitDevServiceProperties extends AbstractBaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.openlit";

    /** Non-distroless image: honors CLICKHOUSE_USER/PASSWORD for Testcontainers. */
    private String clickhouseImageName = "clickhouse/clickhouse-server:24.8";
    private int otlpGrpcPort = 0;
    private int otlpHttpPort = 0;

    public OpenLitDevServiceProperties() {
        setImageName("ghcr.io/openlit/openlit:1.21.1");
        setShared(true);
        setStartupTimeout(java.time.Duration.ofMinutes(2));
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
