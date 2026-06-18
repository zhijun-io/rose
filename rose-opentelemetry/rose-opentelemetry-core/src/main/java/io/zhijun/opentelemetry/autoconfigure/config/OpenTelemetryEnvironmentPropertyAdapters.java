package io.zhijun.opentelemetry.autoconfigure.config;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;

import io.zhijun.spring.core.env.PropertyAdapter;
import io.zhijun.opentelemetry.autoconfigure.OpenTelemetryProperties;
import io.zhijun.opentelemetry.autoconfigure.exporter.OpenTelemetryExporterProperties;
import io.zhijun.opentelemetry.autoconfigure.metrics.OpenTelemetryMetricsProperties;
import io.zhijun.opentelemetry.autoconfigure.metrics.exporter.OpenTelemetryMetricsExporterProperties;
import io.zhijun.opentelemetry.autoconfigure.resource.OpenTelemetryResourceProperties;
import io.zhijun.opentelemetry.autoconfigure.traces.OpenTelemetryPropagationProperties;
import io.zhijun.opentelemetry.autoconfigure.traces.OpenTelemetryTracingProperties;
import io.zhijun.opentelemetry.autoconfigure.traces.exporter.OpenTelemetryTracingExporterProperties;

/**
 * Adapters from the OpenTelemetry Environment Variable Specification to {@code rose.otel.*} properties.
 *
 * @see <a href="https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/">OTel env spec</a>
 */
class OpenTelemetryEnvironmentPropertyAdapters {

    static PropertyAdapter general(ConfigurableEnvironment environment) {
        Assert.notNull(environment, "environment cannot be null");
        return PropertyAdapter.builder(environment)
                .mapProperty("otel.sdk.disabled", OpenTelemetryProperties.CONFIG_PREFIX + ".enabled",
                        value -> !Boolean.parseBoolean(value.toLowerCase()))
                .mapMap("otel.resource.attributes", OpenTelemetryResourceProperties.CONFIG_PREFIX + ".attributes")
                .mapString("otel.service.name", OpenTelemetryResourceProperties.CONFIG_PREFIX + ".service-name")
                .mapEnum("otel.propagators", OpenTelemetryPropagationProperties.CONFIG_PREFIX + ".produce",
                        OpenTelemetryEnvironmentPropertyConverters::propagationType)
                .mapEnum("otel.tracer.sampler", OpenTelemetryTracingProperties.CONFIG_PREFIX + ".sampling.strategy",
                        OpenTelemetryEnvironmentPropertyConverters::samplingStrategy)
                .mapDouble("otel.tracer.sampler.arg", OpenTelemetryTracingProperties.CONFIG_PREFIX + ".sampling.probability")
                .build();
    }

    static PropertyAdapter batchSpanProcessor(ConfigurableEnvironment environment) {
        Assert.notNull(environment, "environment cannot be null");
        return PropertyAdapter.builder(environment)
                .mapDuration("otel.bsp.schedule.delay",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.schedule-delay")
                .mapDuration("otel.bsp.export.timeout",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.export-timeout")
                .mapInteger("otel.bsp.max.queue.size",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.max-queue-size")
                .mapInteger("otel.bsp.max.export.batch.size",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.max-export-batch-size")
                .build();
    }

    static PropertyAdapter attributeLimits(ConfigurableEnvironment environment) {
        Assert.notNull(environment, "environment cannot be null");
        return PropertyAdapter.builder(environment)
                .mapInteger("otel.attribute.value.length.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-attribute-value-length")
                .mapInteger("otel.attribute.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-attributes")
                .build();
    }

    static PropertyAdapter spanLimits(ConfigurableEnvironment environment) {
        Assert.notNull(environment, "environment cannot be null");
        return PropertyAdapter.builder(environment)
                .mapInteger("otel.span.attribute.value.length.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-attribute-value-length")
                .mapInteger("otel.span.attribute.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-attributes")
                .mapInteger("otel.span.event.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-events")
                .mapInteger("otel.span.link.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-links")
                .mapInteger("otel.event.attribute.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-attributes-per-event")
                .mapInteger("otel.link.attribute.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-attributes-per-link")
                .build();
    }

    static PropertyAdapter exporterSelection(ConfigurableEnvironment environment) {
        Assert.notNull(environment, "environment cannot be null");
        return PropertyAdapter.builder(environment)
                .mapEnum("otel.traces.exporter", OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".type",
                        OpenTelemetryEnvironmentPropertyConverters::exporterType)
                .mapEnum("otel.metrics.exporter", OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".type",
                        OpenTelemetryEnvironmentPropertyConverters::exporterType)
                .build();
    }

    static PropertyAdapter metrics(ConfigurableEnvironment environment) {
        Assert.notNull(environment, "environment cannot be null");
        return PropertyAdapter.builder(environment)
                .mapEnum("otel.metrics.exemplar.filter", OpenTelemetryMetricsProperties.CONFIG_PREFIX + ".exemplars.filter",
                        OpenTelemetryEnvironmentPropertyConverters::exemplarFilter)
                .mapDuration("otel.metric.export.interval", OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".interval")
                .mapDuration("otel.metric.export.timeout",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.timeout")
                .build();
    }

    static PropertyAdapter otlpExporter(ConfigurableEnvironment environment) {
        Assert.notNull(environment, "environment cannot be null");
        return PropertyAdapter.builder(environment)
                .mapEnum("otel.exporter.otlp.protocol", OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.protocol",
                        OpenTelemetryEnvironmentPropertyConverters::protocol)
                .mapString("otel.exporter.otlp.endpoint", OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.endpoint")
                .mapMap("otel.exporter.otlp.headers", OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.headers")
                .mapEnum("otel.exporter.otlp.compression", OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.compression",
                        OpenTelemetryEnvironmentPropertyConverters::compression)
                .mapDuration("otel.exporter.otlp.timeout", OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.timeout")
                .mapEnum("otel.exporter.otlp.metrics.protocol",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.protocol",
                        OpenTelemetryEnvironmentPropertyConverters::protocol)
                .mapString("otel.exporter.otlp.metrics.endpoint",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.endpoint")
                .mapMap("otel.exporter.otlp.metrics.headers",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.headers")
                .mapEnum("otel.exporter.otlp.metrics.compression",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.compression",
                        OpenTelemetryEnvironmentPropertyConverters::compression)
                .mapDuration("otel.exporter.otlp.metrics.timeout",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.timeout")
                .mapEnum("otel.exporter.otlp.traces.protocol",
                        OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".otlp.protocol",
                        OpenTelemetryEnvironmentPropertyConverters::protocol)
                .mapString("otel.exporter.otlp.traces.endpoint",
                        OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".otlp.endpoint")
                .mapMap("otel.exporter.otlp.traces.headers",
                        OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".otlp.headers")
                .mapEnum("otel.exporter.otlp.traces.compression",
                        OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".otlp.compression",
                        OpenTelemetryEnvironmentPropertyConverters::compression)
                .mapDuration("otel.exporter.otlp.traces.timeout",
                        OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".otlp.timeout")
                .build();
    }
}
