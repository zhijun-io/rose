package io.zhijun.observability.otel.autoconfigure.traces;

import java.util.List;
import java.util.stream.Collectors;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.zhijun.observability.otel.autoconfigure.traces.propagation.OpenTelemetryPropagationConfiguration;

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

    public static final String INSTRUMENTATION_SCOPE_NAME = "org.springframework.boot";

    @Bean
    @ConditionalOnMissingBean
    SdkTracerProvider tracerProvider(Clock clock,
                                     Resource resource,
                                     Sampler sampler,
                                     SpanLimits spanLimits,
                                     ObjectProvider<SpanProcessor> spanProcessors,
                                     ObjectProvider<OpenTelemetryTracerProviderBuilderCustomizer> customizers) {
        SdkTracerProviderBuilder builder = SdkTracerProvider.builder()
                .setResource(resource)
                .setSampler(sampler)
                .setClock(clock)
                .setSpanLimits(spanLimits);
        spanProcessors.orderedStream().forEach(builder::addSpanProcessor);
        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    Sampler sampler(OpenTelemetryTracingProperties properties) {
        switch (properties.getSampling().getStrategy()) {
            case ALWAYS_ON:
                return Sampler.alwaysOn();
            case ALWAYS_OFF:
                return Sampler.alwaysOff();
            case TRACE_ID_RATIO:
                return Sampler.traceIdRatioBased(properties.getSampling().getProbability());
            case PARENT_BASED_ALWAYS_ON:
                return Sampler.parentBased(Sampler.alwaysOn());
            case PARENT_BASED_ALWAYS_OFF:
                return Sampler.parentBased(Sampler.alwaysOff());
            case PARENT_BASED_TRACE_ID_RATIO:
                return Sampler.parentBased(Sampler.traceIdRatioBased(properties.getSampling().getProbability()));
            default:
                return Sampler.parentBased(Sampler.traceIdRatioBased(properties.getSampling().getProbability()));
        }
    }

    @Bean
    @ConditionalOnMissingBean
    SpanLimits spanLimits(OpenTelemetryTracingProperties properties) {
        return SpanLimits.builder()
                .setMaxAttributeValueLength(properties.getLimits().getMaxAttributeValueLength())
                .setMaxNumberOfAttributes(properties.getLimits().getMaxNumberOfAttributes())
                .setMaxNumberOfEvents(properties.getLimits().getMaxNumberOfEvents())
                .setMaxNumberOfLinks(properties.getLimits().getMaxNumberOfLinks())
                .setMaxNumberOfAttributesPerEvent(properties.getLimits().getMaxNumberOfAttributesPerEvent())
                .setMaxNumberOfAttributesPerLink(properties.getLimits().getMaxNumberOfAttributesPerLink())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(SpanExporter.class)
    BatchSpanProcessor batchSpanProcessor(OpenTelemetryTracingProperties properties,
                                        ObjectProvider<SpanExporter> spanExporters,
                                        ObjectProvider<MeterProvider> meterProvider) {
        List<SpanExporter> exporters = spanExporters.orderedStream().collect(Collectors.toList());
        SpanExporter composite = SpanExporter.composite(exporters);
        BatchSpanProcessorBuilder spanProcessorBuilder = BatchSpanProcessor.builder(composite)
                .setExportUnsampledSpans(properties.getProcessor().isExportUnsampledSpans())
                .setExporterTimeout(properties.getProcessor().getExportTimeout())
                .setScheduleDelay(properties.getProcessor().getScheduleDelay())
                .setMaxExportBatchSize(properties.getProcessor().getMaxExportBatchSize())
                .setMaxQueueSize(properties.getProcessor().getMaxQueueSize());
        if (properties.getProcessor().isMetrics()) {
            meterProvider.ifAvailable(spanProcessorBuilder::setMeterProvider);
        }
        return spanProcessorBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    ContextPropagators contextPropagators(ObjectProvider<TextMapPropagator> textMapPropagators) {
        List<TextMapPropagator> propagators = textMapPropagators.orderedStream().collect(Collectors.toList());
        return ContextPropagators.create(TextMapPropagator.composite(propagators));
    }

    @Bean
    @ConditionalOnMissingBean
    Tracer otelTracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(INSTRUMENTATION_SCOPE_NAME, SpringBootVersion.getVersion());
    }
}
