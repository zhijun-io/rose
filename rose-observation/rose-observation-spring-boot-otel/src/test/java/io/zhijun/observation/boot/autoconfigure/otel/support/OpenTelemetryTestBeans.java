package io.zhijun.observation.boot.autoconfigure.otel.support;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.resources.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Common beans for OpenTelemetry auto-configuration unit test (Boot 2.7 has no {@code ApplicationContextRunner#withBean}).
 */
@Configuration(proxyBeanMethods = false)
public final class OpenTelemetryTestBeans {

    @Bean
    Clock clock() {
        return Clock.getDefault();
    }

    @Bean
    Resource resource() {
        return Resource.empty();
    }

    @Bean
    OpenTelemetry openTelemetry() {
        return OpenTelemetry.noop();
    }
}
