package io.zhijun.observation.boot.autoconfigure.otel.support;

import io.opentelemetry.sdk.trace.export.SpanExporter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans required by tracing auto-configuration unit test.
 */
@Configuration(proxyBeanMethods = false)
public final class TracingTestBeans {

    @Bean
    SpanExporter spanExporter() {
        return OtelExporterMocks.spanExporter();
    }
}
