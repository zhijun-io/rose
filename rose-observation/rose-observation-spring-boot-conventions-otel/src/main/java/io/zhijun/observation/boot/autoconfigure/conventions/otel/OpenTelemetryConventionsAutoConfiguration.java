package io.zhijun.observation.boot.autoconfigure.conventions.otel;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observation.core.TelemetryConventionsBackend;
import io.zhijun.observation.boot.autoconfigure.otel.resource.OpenTelemetryResourceBuilderCustomizer;

/**
 * Auto-configuration for OpenTelemetry Semantic Conventions.
 */
@AutoConfiguration
@EnableConfigurationProperties(OpenTelemetryConventionsProperties.class)
public final class OpenTelemetryConventionsAutoConfiguration {

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
