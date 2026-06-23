package io.zhijun.devservice.openlit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import io.zhijun.devservice.core.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.opentelemetry.autoconfigure.exporter.otlp.OtlpContainerConnectionDetails;
import io.zhijun.opentelemetry.autoconfigure.metrics.exporter.OpenTelemetryMetricsExporterAutoConfiguration;
import io.zhijun.opentelemetry.autoconfigure.metrics.exporter.otlp.OtlpMetricsConnectionDetails;
import io.zhijun.opentelemetry.autoconfigure.traces.exporter.OpenTelemetryTracingExporterAutoConfiguration;
import io.zhijun.opentelemetry.autoconfigure.traces.exporter.otlp.OtlpTracingConnectionDetails;

/**
 * Registers OTLP connection details from the OpenLit dev service container.
 */
@AutoConfiguration
@ConditionalOnBean(RoseOpenLitContainer.class)
@AutoConfigureAfter({ DevServiceAutoConfiguration.class, OpenLitDevServicesAutoConfiguration.class })
@AutoConfigureBefore({ OpenTelemetryTracingExporterAutoConfiguration.class, OpenTelemetryMetricsExporterAutoConfiguration.class })
class OpenLitOtlpConnectionDetailsConfiguration {

    @Bean
    @ConditionalOnMissingBean(OtlpTracingConnectionDetails.class)
    OtlpTracingConnectionDetails openLitOtlpTracingConnectionDetails(RoseOpenLitContainer container) {
        ensureRunning(container);
        return OtlpContainerConnectionDetails.tracing(container.getHost(), container.getOtlpHttpPort(),
                container.getOtlpGrpcPort());
    }

    @Bean
    @ConditionalOnMissingBean(OtlpMetricsConnectionDetails.class)
    OtlpMetricsConnectionDetails openLitOtlpMetricsConnectionDetails(RoseOpenLitContainer container) {
        ensureRunning(container);
        return OtlpContainerConnectionDetails.metrics(container.getHost(), container.getOtlpHttpPort(),
                container.getOtlpGrpcPort());
    }

    private static void ensureRunning(RoseOpenLitContainer container) {
        if (!container.isRunning()) {
            container.start();
        }
    }
}
