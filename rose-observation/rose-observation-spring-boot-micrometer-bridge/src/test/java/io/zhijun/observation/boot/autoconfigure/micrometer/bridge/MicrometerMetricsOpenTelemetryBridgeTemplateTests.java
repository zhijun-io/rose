package io.zhijun.observation.boot.autoconfigure.micrometer.bridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.micrometer.v1_5.OpenTelemetryMeterRegistry;

import org.junit.jupiter.api.Test;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;

class MicrometerMetricsOpenTelemetryBridgeTemplateTests {

    private final MicrometerMetricsOpenTelemetryBridgeTemplate template =
            new MicrometerMetricsOpenTelemetryBridgeTemplate();

    @Test
    void buildSimpleMeterRegistryUsesExporterInterval() {
        OpenTelemetryMetricsExporterProperties properties = new OpenTelemetryMetricsExporterProperties();
        properties.setInterval(Duration.ofSeconds(7));

        SimpleMeterRegistry registry = template.buildSimpleMeterRegistry(Clock.SYSTEM, properties);

        assertThat(registry.config().clock()).isEqualTo(Clock.SYSTEM);
    }

    @Test
    void buildMeterRegistryAppliesBridgeProperties() {
        MicrometerMetricsOpenTelemetryBridgeProperties properties =
                new MicrometerMetricsOpenTelemetryBridgeProperties();
        properties.setBaseTimeUnit(TimeUnit.MILLISECONDS);
        properties.setHistogramGauges(false);

        MeterRegistry registry = template.buildMeterRegistry(properties, Clock.SYSTEM, OpenTelemetry.noop());

        assertThat(registry).isInstanceOf(OpenTelemetryMeterRegistry.class);
    }
}
