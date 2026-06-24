package io.zhijun.observation.boot.autoconfigure.otel.resource;

import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;

/**
 * Customizes the {@link ResourceBuilder} used to build the autoconfigured {@link Resource}.
 */
@FunctionalInterface
public interface OpenTelemetryResourceBuilderCustomizer {

    void customize(ResourceBuilder builder);

}
