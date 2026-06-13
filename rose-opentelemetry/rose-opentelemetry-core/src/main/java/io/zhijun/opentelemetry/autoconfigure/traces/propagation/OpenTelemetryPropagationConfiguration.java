package io.zhijun.opentelemetry.autoconfigure.traces.propagation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.context.propagation.TextMapPropagator;

import io.zhijun.opentelemetry.autoconfigure.traces.OpenTelemetryPropagationProperties;
import io.zhijun.opentelemetry.autoconfigure.traces.exporter.ConditionalOnOpenTelemetryTracingExporter;

/**
 * OpenTelemetry trace propagation (W3C / B3) without Micrometer tracing bridge.
 */
public final class OpenTelemetryPropagationConfiguration {

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(OpenTelemetryPropagationProperties.class)
    public static class PropagationConfiguration {

        @Bean
        @ConditionalOnMissingBean(TextMapPropagator.class)
        @ConditionalOnOpenTelemetryTracingExporter("otlp")
        TextMapPropagator textMapPropagator(OpenTelemetryPropagationProperties properties) {
            return CompositeTextMapPropagator.create(properties);
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class NoPropagation {

        @Bean
        @ConditionalOnMissingBean(TextMapPropagator.class)
        TextMapPropagator noopTextMapPropagator() {
            return TextMapPropagator.noop();
        }
    }
}
