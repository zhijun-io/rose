package io.zhijun.devservice.boot.autoconfigure.otel;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.container.DevServiceContainerLifecycle;
import io.zhijun.observation.boot.autoconfigure.otel.common.OtlpContainerConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OtlpMetricsConnectionDetails;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryTracingExporterAutoConfiguration;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OtlpTracingConnectionDetails;

/**
 * Registers OTLP connection details from the OpenTelemetry Collector dev service container.
 */
@AutoConfiguration
@ConditionalOnBean(OtelCollectorContainer.class)
@AutoConfigureAfter({DevServiceAutoConfiguration.class, OtelCollectorDevServicesAutoConfiguration.class})
@AutoConfigureBefore({
    OpenTelemetryTracingExporterAutoConfiguration.class,
    OpenTelemetryMetricsExporterAutoConfiguration.class
})
class OtelCollectorOtlpConnectionDetailsConfiguration {

    @Bean
    @ConditionalOnMissingBean(OtlpTracingConnectionDetails.class)
    OtlpTracingConnectionDetails otelCollectorOtlpTracingConnectionDetails(OtelCollectorContainer container) {
        DevServiceContainerLifecycle.startIfNecessary(container);
        return OtlpContainerConnectionDetails.tracing(
                container.getHost(), container.getHttpPort(), container.getGrpcPort());
    }

    @Bean
    @ConditionalOnMissingBean(OtlpMetricsConnectionDetails.class)
    OtlpMetricsConnectionDetails otelCollectorOtlpMetricsConnectionDetails(OtelCollectorContainer container) {
        DevServiceContainerLifecycle.startIfNecessary(container);
        return OtlpContainerConnectionDetails.metrics(
                container.getHost(), container.getHttpPort(), container.getGrpcPort());
    }
}
