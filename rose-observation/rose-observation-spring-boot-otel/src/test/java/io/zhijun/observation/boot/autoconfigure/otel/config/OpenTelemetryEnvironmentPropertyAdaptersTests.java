package io.zhijun.observation.boot.autoconfigure.otel.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import io.zhijun.observation.boot.autoconfigure.otel.OpenTelemetryProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.ExporterType;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Compression;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.resource.OpenTelemetryResourceProperties;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryPropagationProperties.PropagationType;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryTracingProperties;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryTracingProperties.SamplingStrategy;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.OpenTelemetryTracingExporterProperties;

class OpenTelemetryEnvironmentPropertyAdaptersTests {

    @Test
    void generalShouldMapProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("otel.sdk.disabled", "true")
                .withProperty("otel.resource.attributes", "key1=value1,key2=value2")
                .withProperty("otel.service.name", "test-service")
                .withProperty("otel.propagators", "tracecontext,b3")
                .withProperty("otel.tracer.sampler", "traceidratio")
                .withProperty("otel.tracer.sampler.arg", "0.5");

        Map<String, Object> properties =
                OpenTelemetryEnvironmentPropertyAdapters.general(environment).getRoseProperties();

        assertThat(properties.get(OpenTelemetryProperties.ENABLED_PROPERTY)).isEqualTo(false);
        assertThat(properties.get(OpenTelemetryResourceProperties.ATTRIBUTES_PROPERTY))
                .isEqualTo(mapOf("key1", "value1", "key2", "value2"));
        assertThat(properties.get(OpenTelemetryResourceProperties.SERVICE_NAME_PROPERTY))
                .isEqualTo("test-service");
        assertThat((List<PropagationType>) properties.get("rose.otel.traces.propagation.produce"))
                .containsExactlyInAnyOrder(PropagationType.W3C, PropagationType.B3);
        assertThat(properties.get(OpenTelemetryTracingProperties.CONFIG_PREFIX + ".sampling.strategy"))
                .isEqualTo(SamplingStrategy.TRACE_ID_RATIO);
        assertThat(properties.get(OpenTelemetryTracingProperties.CONFIG_PREFIX + ".sampling.probability"))
                .isEqualTo(0.5);
    }

    @Test
    void generalShouldThrowExceptionWhenEnvironmentIsNull() {
        assertThatThrownBy(new org.assertj.core.api.ThrowableAssert.ThrowingCallable() {
                    @Override
                    public void call() {
                        OpenTelemetryEnvironmentPropertyAdapters.general(null);
                    }
                })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("environment cannot be null");
    }

    @Test
    void batchSpanProcessorShouldMapProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("otel.bsp.schedule.delay", "5000")
                .withProperty("otel.bsp.max.queue.size", "2048")
                .withProperty("otel.bsp.max.export.batch.size", "512")
                .withProperty("otel.bsp.export.timeout", "30000");

        Map<String, Object> properties = OpenTelemetryEnvironmentPropertyAdapters.batchSpanProcessor(environment)
                .getRoseProperties();

        assertThat(properties.get(OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.schedule-delay"))
                .isEqualTo(Duration.ofSeconds(5));
        assertThat(properties.get(OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.max-queue-size"))
                .isEqualTo(2048);
        assertThat(properties.get(OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.max-export-batch-size"))
                .isEqualTo(512);
        assertThat(properties.get(OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.export-timeout"))
                .isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void otlpExporterShouldMapProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("otel.exporter.otlp.protocol", "grpc")
                .withProperty("otel.exporter.otlp.endpoint", "http://collector:4317")
                .withProperty("otel.exporter.otlp.headers", "api-key=secret")
                .withProperty("otel.exporter.otlp.compression", "gzip")
                .withProperty("otel.exporter.otlp.timeout", "10000")
                .withProperty("otel.exporter.otlp.traces.endpoint", "http://collector:4318/v1/traces");

        Map<String, Object> properties = OpenTelemetryEnvironmentPropertyAdapters.otlpExporter(environment)
                .getRoseProperties();

        assertThat(properties.get(OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.protocol"))
                .isEqualTo(Protocol.GRPC);
        assertThat(properties.get(OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.endpoint"))
                .isEqualTo("http://collector:4317");
        assertThat(properties.get(OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.headers"))
                .isEqualTo(singleEntryMap("api-key", "secret"));
        assertThat(properties.get(OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.compression"))
                .isEqualTo(Compression.GZIP);
        assertThat(properties.get(OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.timeout"))
                .isEqualTo(Duration.ofSeconds(10));
        assertThat(properties.get(OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".otlp.endpoint"))
                .isEqualTo("http://collector:4318/v1/traces");
    }

    @Test
    void exporterSelectionShouldMapProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("otel.traces.exporter", "otlp")
                .withProperty("otel.metrics.exporter", "none");

        Map<String, Object> properties = OpenTelemetryEnvironmentPropertyAdapters.exporterSelection(environment)
                .getRoseProperties();

        assertThat(properties.get(OpenTelemetryTracingExporterProperties.TYPE_PROPERTY))
                .isEqualTo(ExporterType.OTLP);
        assertThat(properties.get(OpenTelemetryMetricsExporterProperties.TYPE_PROPERTY))
                .isEqualTo(ExporterType.NONE);
    }

    @Test
    void metricsShouldMapProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("otel.metrics.exemplar.filter", "trace_based")
                .withProperty("otel.metric.export.interval", "60000")
                .withProperty("otel.metric.export.timeout", "30000");

        Map<String, Object> properties =
                OpenTelemetryEnvironmentPropertyAdapters.metrics(environment).getRoseProperties();

        assertThat(properties.get(OpenTelemetryMetricsProperties.CONFIG_PREFIX + ".exemplars.filter"))
                .isEqualTo(OpenTelemetryMetricsProperties.ExemplarFilter.TRACE_BASED);
        assertThat(properties.get(OpenTelemetryMetricsExporterProperties.INTERVAL_PROPERTY))
                .isEqualTo(Duration.ofSeconds(60));
        assertThat(properties.get(OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.timeout"))
                .isEqualTo(Duration.ofSeconds(30));
    }

    private static Map<String, String> singleEntryMap(String key, String value) {
        Map<String, String> map = new java.util.HashMap<String, String>();
        map.put(key, value);
        return map;
    }

    private static Map<String, String> mapOf(String key1, String value1, String key2, String value2) {
        Map<String, String> map = new java.util.HashMap<String, String>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }
}
