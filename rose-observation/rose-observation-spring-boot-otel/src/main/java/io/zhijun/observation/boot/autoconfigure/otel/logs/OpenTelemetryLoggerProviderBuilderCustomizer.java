package io.zhijun.observation.boot.autoconfigure.otel.logs;

import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.zhijun.observation.boot.autoconfigure.otel.OpenTelemetryBuilderCustomizer;

/**
 * Customizes the {@link SdkLoggerProviderBuilder} used to build the autoconfigured {@link SdkLoggerProvider}.
 */
@FunctionalInterface
public interface OpenTelemetryLoggerProviderBuilderCustomizer
        extends OpenTelemetryBuilderCustomizer<SdkLoggerProviderBuilder> {
}
