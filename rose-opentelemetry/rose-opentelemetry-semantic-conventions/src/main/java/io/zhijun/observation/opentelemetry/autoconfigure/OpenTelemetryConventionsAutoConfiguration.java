package io.zhijun.observation.opentelemetry.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observation.conventions.AiObservationConventionsProvider;
import io.zhijun.opentelemetry.autoconfigure.resource.OpenTelemetryResourceBuilderCustomizer;

/**
 * Auto-configuration for OpenTelemetry Semantic Conventions.
 */
@AutoConfiguration
@EnableConfigurationProperties(OpenTelemetryConventionsProperties.class)
public final class OpenTelemetryConventionsAutoConfiguration {

    @Bean
    AiObservationConventionsProvider openTelemetryAiObservationConventionsProvider() {
        return AiObservationConventionsProvider.of("opentelemetry");
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

