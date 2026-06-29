package io.zhijun.observation.boot.autoconfigure.micrometer.bridge;

import java.time.Duration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.CountingMode;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.micrometer.v1_5.OpenTelemetryMeterRegistry;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;

/**
 * Builds Micrometer meter registries for the OpenTelemetry bridge.
 */
public final class MicrometerMetricsOpenTelemetryBridgeTemplate {

    public SimpleMeterRegistry buildSimpleMeterRegistry(
            Clock clock, OpenTelemetryMetricsExporterProperties properties) {
        return new SimpleMeterRegistry(new OpenTelemetrySimpleConfig(properties), clock);
    }

    public MeterRegistry buildMeterRegistry(
            MicrometerMetricsOpenTelemetryBridgeProperties properties, Clock clock, OpenTelemetry openTelemetry) {
        return OpenTelemetryMeterRegistry.builder(openTelemetry)
                .setBaseTimeUnit(properties.getBaseTimeUnit())
                .setClock(clock)
                .setMicrometerHistogramGaugesEnabled(properties.isHistogramGauges())
                .setPrometheusMode(false)
                .build();
    }

    static final class OpenTelemetrySimpleConfig implements SimpleConfig {

        private final OpenTelemetryMetricsExporterProperties properties;

        OpenTelemetrySimpleConfig(OpenTelemetryMetricsExporterProperties properties) {
            this.properties = properties;
        }

        @Override
        public String get(String key) {
            return "";
        }

        @Override
        public Duration step() {
            return properties.getInterval();
        }

        @Override
        public CountingMode mode() {
            return CountingMode.CUMULATIVE;
        }
    }
}
