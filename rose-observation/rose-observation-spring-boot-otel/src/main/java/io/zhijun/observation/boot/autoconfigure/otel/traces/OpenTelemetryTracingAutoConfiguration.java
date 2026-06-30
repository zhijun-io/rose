package io.zhijun.observation.boot.autoconfigure.otel.traces;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryPropagationConfiguration;

/**
 * Auto-configuration for OpenTelemetry tracing (SDK export without Micrometer tracing bridge).
 */
@AutoConfiguration
@ConditionalOnOpenTelemetryTracing
@EnableConfigurationProperties(OpenTelemetryTracingProperties.class)
@Import({
    OpenTelemetryPropagationConfiguration.PropagationConfiguration.class,
    OpenTelemetryPropagationConfiguration.NoPropagation.class
})
public final class OpenTelemetryTracingAutoConfiguration {
    private final OpenTelemetryTracingTemplate template = new OpenTelemetryTracingTemplate();

    @Bean
    @ConditionalOnMissingBean
    SdkTracerProvider tracerProvider(
            Clock clock,
            Resource resource,
            Sampler sampler,
            SpanLimits spanLimits,
            ObjectProvider<SpanProcessor> spanProcessors,
            ObjectProvider<OpenTelemetryTracerProviderBuilderCustomizer> customizers) {
        return template.buildTracerProvider(clock, resource, sampler, spanLimits, spanProcessors, customizers);
    }

    @Bean
    @ConditionalOnMissingBean
    Sampler sampler(OpenTelemetryTracingProperties properties) {
        return template.sampler(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    SpanLimits spanLimits(OpenTelemetryTracingProperties properties) {
        return template.spanLimits(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(SpanExporter.class)
    BatchSpanProcessor batchSpanProcessor(
            OpenTelemetryTracingProperties properties,
            ObjectProvider<SpanExporter> spanExporters,
            ObjectProvider<MeterProvider> meterProvider) {
        return template.batchSpanProcessor(properties, spanExporters, meterProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    ContextPropagators contextPropagators(ObjectProvider<TextMapPropagator> textMapPropagators) {
        return template.contextPropagators(textMapPropagators);
    }

    @Bean
    @ConditionalOnMissingBean
    Tracer otelTracer(OpenTelemetry openTelemetry) {
        return template.tracer(openTelemetry);
    }
}
