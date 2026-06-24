package io.zhijun.observation.boot.autoconfigure.otel.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.sdk.trace.export.SpanExporter;

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
