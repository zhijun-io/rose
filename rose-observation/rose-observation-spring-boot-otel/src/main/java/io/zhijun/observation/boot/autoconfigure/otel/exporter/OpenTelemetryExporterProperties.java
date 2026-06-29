package io.zhijun.observation.boot.autoconfigure.otel.exporter;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import io.opentelemetry.sdk.common.export.MemoryMode;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.lang.Nullable;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Compression;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.ProxyConfig;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.RetryConfig;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.TlsConfig;

/**
 * Configuration properties for OpenTelemetry exporters.
 */
@ConfigurationProperties(prefix = OpenTelemetryExporterProperties.CONFIG_PREFIX)
public class OpenTelemetryExporterProperties {

    public static final String CONFIG_PREFIX = "rose.otel.exporter";

    public static final String TYPE_PROPERTY = CONFIG_PREFIX + ".type";

    public static final String OTLP_CONFIG_PREFIX = CONFIG_PREFIX + ".otlp";

    public static final String MICROMETER_REGISTRY_CONFIG_PREFIX = OTLP_CONFIG_PREFIX + ".micrometer";

    public static final String MICROMETER_REGISTRY_ENABLED_PROPERTY = MICROMETER_REGISTRY_CONFIG_PREFIX + ".enabled";

    /**
     * The type of OpenTelemetry exporter to use.
     */
    private ExporterType type = ExporterType.OTLP;

    /**
     * Common options for the OTLP exporters.
     */
    private final Otlp otlp = new Otlp();

    /**
     * Whether to reuse objects to reduce allocation or work with immutable data structures.
     */
    private MemoryMode memoryMode = MemoryMode.REUSABLE_DATA;

    public ExporterType getType() {
        return type;
    }

    public void setType(ExporterType type) {
        this.type = type;
    }

    public Otlp getOtlp() {
        return otlp;
    }

    public MemoryMode getMemoryMode() {
        return memoryMode;
    }

    public void setMemoryMode(MemoryMode memoryMode) {
        this.memoryMode = memoryMode;
    }

    /**
     * Configuration properties for exporting OpenTelemetry telemetry data using OTLP.
     */
    public static class Otlp {

        /**
         * The endpoint to which telemetry data will be sent.
         */
        @Nullable
        private URI endpoint;

        /**
         * The maximum waiting time for the exporter to send each telemetry batch.
         */
        private Duration timeout = Duration.ofSeconds(10);

        /**
         * The maximum waiting time for the exporter to establish a connection to the endpoint.
         */
        private Duration connectTimeout = Duration.ofSeconds(10);

        /**
         * Transport protocol to use for OTLP requests.
         */
        private Protocol protocol = Protocol.HTTP_PROTOBUF;

        /**
         * Compression type to use for OTLP requests.
         */
        private Compression compression = Compression.GZIP;

        /**
         * Configuration for retrying failed requests.
         */
        @NestedConfigurationProperty
        private final RetryConfig retry = new RetryConfig();

        /**
         * Additional headers to include in each request to the endpoint.
         */
        private Map<String, String> headers = new HashMap<>();

        /**
         * Whether to generate metrics for the exporter.
         */
        private boolean metrics = false;

        /**
         * TLS settings for OTLP exporters.
         */
        @NestedConfigurationProperty
        private final TlsConfig tls = new TlsConfig();

        /**
         * HTTP proxy settings for OTLP exporters.
         */
        @NestedConfigurationProperty
        private final ProxyConfig proxy = new ProxyConfig();

        @Nullable
        public URI getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(URI endpoint) {
            this.endpoint = endpoint;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Protocol getProtocol() {
            return protocol;
        }

        public void setProtocol(Protocol protocol) {
            this.protocol = protocol;
        }

        public Compression getCompression() {
            return compression;
        }

        public void setCompression(Compression compression) {
            this.compression = compression;
        }

        public RetryConfig getRetry() {
            return retry;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public boolean isMetrics() {
            return metrics;
        }

        public void setMetrics(boolean metrics) {
            this.metrics = metrics;
        }

        public TlsConfig getTls() {
            return tls;
        }

        public ProxyConfig getProxy() {
            return proxy;
        }
    }
}
