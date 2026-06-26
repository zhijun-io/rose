package io.zhijun.devservice.boot.autoconfigure.otel;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.container.DevServiceContainerLifecycle;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.OtlpContainerConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.OpenTelemetryMetricsExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.otlp.OtlpMetricsConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.OpenTelemetryTracingExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.traces.exporter.otlp.OtlpTracingConnectionDetails;

/**
 * Registers OTLP connection details from the OpenTelemetry Collector dev service container.
 */
@AutoConfiguration
@ConditionalOnBean(OtelCollectorContainer.class)
@AutoConfigureAfter({ DevServiceAutoConfiguration.class, OtelCollectorDevServicesAutoConfiguration.class })
@AutoConfigureBefore({ OpenTelemetryTracingExporterAutoConfiguration.class, OpenTelemetryMetricsExporterAutoConfiguration.class })
class OtelCollectorOtlpConnectionDetailsConfiguration {

    @Bean
    @ConditionalOnMissingBean(OtlpTracingConnectionDetails.class)
    OtlpTracingConnectionDetails otelCollectorOtlpTracingConnectionDetails(OtelCollectorContainer container) {
        DevServiceContainerLifecycle.startIfNecessary(container);
        return OtlpContainerConnectionDetails.tracing(container.getHost(), container.getHttpPort(),
                container.getGrpcPort());
    }

    @Bean
    @ConditionalOnMissingBean(OtlpMetricsConnectionDetails.class)
    OtlpMetricsConnectionDetails otelCollectorOtlpMetricsConnectionDetails(OtelCollectorContainer container) {
        DevServiceContainerLifecycle.startIfNecessary(container);
        return OtlpContainerConnectionDetails.metrics(container.getHost(), container.getHttpPort(),
                container.getGrpcPort());
    }

}
