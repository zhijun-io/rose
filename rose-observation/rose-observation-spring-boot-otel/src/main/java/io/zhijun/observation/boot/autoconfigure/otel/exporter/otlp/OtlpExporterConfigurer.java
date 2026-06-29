package io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp;

import java.time.Duration;
import java.util.Locale;
import java.util.function.BiConsumer;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.export.MemoryMode;

import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;

/**
 * Shared OTLP exporter builder settings for traces, metrics, and logs.
 */
public final class OtlpExporterConfigurer {

    private OtlpExporterConfigurer() {}

    public static Duration timeout(
            OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        return signalProperties.getTimeout() != null
                ? signalProperties.getTimeout()
                : commonProperties.getOtlp().getTimeout();
    }

    public static Duration connectTimeout(
            OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        return signalProperties.getConnectTimeout() != null
                ? signalProperties.getConnectTimeout()
                : commonProperties.getOtlp().getConnectTimeout();
    }

    public static String compression(
            OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        Compression compression = signalProperties.getCompression() != null
                ? signalProperties.getCompression()
                : commonProperties.getOtlp().getCompression();
        return compression.name().toLowerCase(Locale.ROOT);
    }

    public static MemoryMode memoryMode(OpenTelemetryExporterProperties commonProperties) {
        return commonProperties.getMemoryMode();
    }

    public static io.opentelemetry.sdk.common.export.RetryPolicy retryPolicy(
            OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        return signalProperties.getRetry() != null
                ? RetryConfig.buildRetryPolicy(signalProperties.getRetry())
                : RetryConfig.buildRetryPolicy(commonProperties.getOtlp().getRetry());
    }

    public static void applyHeaders(
            BiConsumer<String, String> headerConsumer,
            OpenTelemetryExporterProperties commonProperties,
            OtlpExporterConfig signalProperties) {
        commonProperties.getOtlp().getHeaders().forEach(headerConsumer);
        signalProperties.getHeaders().forEach(headerConsumer);
    }

    public static void configureExporterMetrics(
            ObjectProvider<MeterProvider> meterProvider,
            OpenTelemetryExporterProperties commonProperties,
            OtlpExporterConfig signalProperties,
            java.util.function.Consumer<MeterProvider> meterProviderConsumer) {
        boolean metricsEnabled =
                signalProperties.isMetrics() != null && Boolean.TRUE.equals(signalProperties.isMetrics())
                        || signalProperties.isMetrics() == null
                                && commonProperties.getOtlp().isMetrics();
        if (metricsEnabled) {
            meterProvider.ifAvailable(meterProviderConsumer);
        }
    }
}
