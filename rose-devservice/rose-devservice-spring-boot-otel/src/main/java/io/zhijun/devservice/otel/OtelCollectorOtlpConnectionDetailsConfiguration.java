package io.zhijun.devservice.otel;

import io.zhijun.devservice.core.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import io.zhijun.observability.otel.autoconfigure.exporter.otlp.OtlpContainerConnectionDetails;
import io.zhijun.observability.otel.autoconfigure.metrics.exporter.OpenTelemetryMetricsExporterAutoConfiguration;
import io.zhijun.observability.otel.autoconfigure.metrics.exporter.otlp.OtlpMetricsConnectionDetails;
import io.zhijun.observability.otel.autoconfigure.traces.exporter.OpenTelemetryTracingExporterAutoConfiguration;
import io.zhijun.observability.otel.autoconfigure.traces.exporter.otlp.OtlpTracingConnectionDetails;

/**
 * Registers OTLP connection details from the OpenTelemetry Collector dev service container.
 */
@AutoConfiguration
@ConditionalOnBean(RoseOtelCollectorContainer.class)
@AutoConfigureAfter({ DevServiceAutoConfiguration.class, OtelCollectorDevServicesAutoConfiguration.class })
@AutoConfigureBefore({ OpenTelemetryTracingExporterAutoConfiguration.class, OpenTelemetryMetricsExporterAutoConfiguration.class })
class OtelCollectorOtlpConnectionDetailsConfiguration {

    @Bean
    @ConditionalOnMissingBean(OtlpTracingConnectionDetails.class)
    OtlpTracingConnectionDetails otelCollectorOtlpTracingConnectionDetails(RoseOtelCollectorContainer container) {
        ensureRunning(container);
        return OtlpContainerConnectionDetails.tracing(container.getHost(), container.getHttpPort(), container.getGrpcPort());
    }

    @Bean
    @ConditionalOnMissingBean(OtlpMetricsConnectionDetails.class)
    OtlpMetricsConnectionDetails otelCollectorOtlpMetricsConnectionDetails(RoseOtelCollectorContainer container) {
        ensureRunning(container);
        return OtlpContainerConnectionDetails.metrics(container.getHost(), container.getHttpPort(), container.getGrpcPort());
    }

    private static void ensureRunning(RoseOtelCollectorContainer container) {
        if (!container.isRunning()) {
            container.start();
        }
    }
}
