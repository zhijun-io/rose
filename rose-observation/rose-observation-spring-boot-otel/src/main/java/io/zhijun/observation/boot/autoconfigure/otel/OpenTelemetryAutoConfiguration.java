package io.zhijun.observation.boot.autoconfigure.otel;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.zhijun.observation.boot.autoconfigure.otel.template.OpenTelemetryTemplate;

/**
 * Auto-configuration for {@link OpenTelemetry}.
 */
@AutoConfiguration
@EnableConfigurationProperties(OpenTelemetryProperties.class)
public final class OpenTelemetryAutoConfiguration {
    private final OpenTelemetryTemplate template = new OpenTelemetryTemplate();

    @Bean
    @ConditionalOnMissingBean(OpenTelemetry.class)
    @ConditionalOnOpenTelemetry
    OpenTelemetrySdk openTelemetrySdk(
            ObjectProvider<SdkLoggerProvider> loggerProvider,
            ObjectProvider<SdkMeterProvider> meterProvider,
            ObjectProvider<SdkTracerProvider> tracerProvider,
            ObjectProvider<ContextPropagators> propagators) {
        return template.buildOpenTelemetrySdk(loggerProvider, meterProvider, tracerProvider, propagators);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnOpenTelemetry(enabled = false)
    OpenTelemetry noopOpenTelemetry() {
        return template.noopOpenTelemetry();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnOpenTelemetry
    Clock clock() {
        return template.clock();
    }

    /**
     * This is needed because Spring Boot doesn't annotation disabling OpenTelemetry
     * and always expects a {@link Resource} bean to be defined.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnOpenTelemetry(enabled = false)
    Resource resource() {
        return template.resource();
    }
}
