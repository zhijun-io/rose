package io.zhijun.observation.boot.autoconfigure.conventions.otel;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observation.boot.autoconfigure.conventions.TelemetryConventionsBackend;
import io.zhijun.observation.boot.autoconfigure.otel.resource.OpenTelemetryResourceBuilderCustomizer;

/**
 * Auto-configuration for OpenTelemetry Semantic Conventions.
 */
@AutoConfiguration
public final class OpenTelemetryConventionsAutoConfiguration {

    static final String CONFIG_PREFIX = "rose.observation.conventions.otel";

    @Bean
    TelemetryConventionsBackend openTelemetryConventionsBackend() {
        return TelemetryConventionsBackend.of("opentelemetry", true);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(OpenTelemetryResourceBuilderCustomizer.class)
    static final class OpenTelemetryResourceConfiguration {

        @Bean
        OpenTelemetryResourceBuilderCustomizer conventionsCustomizer() {
            return builder -> builder.setSchemaUrl("https://opentelemetry.io/schemas/1.27.0");
        }
    }
}
