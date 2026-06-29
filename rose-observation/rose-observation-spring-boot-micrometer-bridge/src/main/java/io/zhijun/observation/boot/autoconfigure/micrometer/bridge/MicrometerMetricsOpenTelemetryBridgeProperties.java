package io.zhijun.observation.boot.autoconfigure.micrometer.bridge;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsProperties;

@ConfigurationProperties(prefix = MicrometerMetricsOpenTelemetryBridgeProperties.CONFIG_PREFIX)
public class MicrometerMetricsOpenTelemetryBridgeProperties {

    public static final String CONFIG_PREFIX = OpenTelemetryMetricsProperties.MICROMETER_BRIDGE_CONFIG_PREFIX;

    public static final String ENABLED_PROPERTY = CONFIG_PREFIX + ".enabled";

    /**
     * Whether to enable the Micrometer Metrics OpenTelemetry bridge.
     */
    private boolean enabled = true;

    /**
     * The base time unit for Micrometer metrics.
     */
    private TimeUnit baseTimeUnit = TimeUnit.SECONDS;

    /**
     * Whether to generate gauge-based Micrometer histograms.
     */
    private boolean histogramGauges = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public TimeUnit getBaseTimeUnit() {
        return baseTimeUnit;
    }

    public void setBaseTimeUnit(TimeUnit baseTimeUnit) {
        this.baseTimeUnit = baseTimeUnit;
    }

    public boolean isHistogramGauges() {
        return histogramGauges;
    }

    public void setHistogramGauges(boolean histogramGauges) {
        this.histogramGauges = histogramGauges;
    }
}
