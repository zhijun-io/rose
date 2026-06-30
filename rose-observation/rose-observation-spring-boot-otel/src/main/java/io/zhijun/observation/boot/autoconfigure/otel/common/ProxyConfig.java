package io.zhijun.observation.boot.autoconfigure.otel.common;

import io.zhijun.core.annotation.Nullable;

/**
 * HTTP proxy settings for OTLP exporters.
 */
public class ProxyConfig {

    /**
     * Proxy host name or IP address.
     */
    @Nullable
    private String host;

    /**
     * Proxy port.
     */
    private int port = 8080;

    @Nullable
    public String getHost() {
        return host;
    }

    public void setHost(@Nullable String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
