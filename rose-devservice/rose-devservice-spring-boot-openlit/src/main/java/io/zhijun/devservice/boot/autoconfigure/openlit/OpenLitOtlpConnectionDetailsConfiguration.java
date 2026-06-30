package io.zhijun.devservice.boot.autoconfigure.openlit;

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
 * Registers OTLP connection details from the OpenLit dev service container.
 */
@AutoConfiguration
@ConditionalOnBean(OpenLitContainer.class)
@AutoConfigureAfter({DevServiceAutoConfiguration.class, OpenLitDevServicesAutoConfiguration.class})
@AutoConfigureBefore({
    OpenTelemetryTracingExporterAutoConfiguration.class,
    OpenTelemetryMetricsExporterAutoConfiguration.class
})
class OpenLitOtlpConnectionDetailsConfiguration {

    @Bean
    @ConditionalOnMissingBean(OtlpTracingConnectionDetails.class)
    OtlpTracingConnectionDetails openLitOtlpTracingConnectionDetails(OpenLitContainer container) {
        DevServiceContainerLifecycle.startIfNecessary(container);
        return OtlpContainerConnectionDetails.tracing(
                container.getHost(), container.getOtlpHttpPort(), container.getOtlpGrpcPort());
    }

    @Bean
    @ConditionalOnMissingBean(OtlpMetricsConnectionDetails.class)
    OtlpMetricsConnectionDetails openLitOtlpMetricsConnectionDetails(OpenLitContainer container) {
        DevServiceContainerLifecycle.startIfNecessary(container);
        return OtlpContainerConnectionDetails.metrics(
                container.getHost(), container.getOtlpHttpPort(), container.getOtlpGrpcPort());
    }
}
