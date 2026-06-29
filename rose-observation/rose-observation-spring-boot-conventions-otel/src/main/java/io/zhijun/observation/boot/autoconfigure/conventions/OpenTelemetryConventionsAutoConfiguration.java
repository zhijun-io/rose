package io.zhijun.observation.boot.autoconfigure.conventions;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observation.boot.autoconfigure.otel.resource.OpenTelemetryResourceBuilderCustomizer;

/**
 * Auto-configuration for OpenTelemetry Semantic Conventions.
 */
@AutoConfiguration
public final class OpenTelemetryConventionsAutoConfiguration {

    private static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.27.0";

    @Bean
    TelemetryConventionsBackend openTelemetryConventionsBackend() {
        return TelemetryConventionsBackend.of("opentelemetry", true);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(OpenTelemetryResourceBuilderCustomizer.class)
    static final class OpenTelemetryResourceConfiguration {

        @Bean
        OpenTelemetryResourceBuilderCustomizer conventionsCustomizer() {
            return builder -> builder.setSchemaUrl(SCHEMA_URL);
        }
    }
}
