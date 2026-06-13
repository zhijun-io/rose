package io.zhijun.opentelemetry.autoconfigure.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.sdk.trace.export.SpanExporter;

import io.zhijun.opentelemetry.autoconfigure.support.OtelExporterMocks;

/**
 * Beans required by tracing auto-configuration unit tests.
 */
@Configuration(proxyBeanMethods = false)
public final class TracingTestBeans {

    @Bean
    SpanExporter spanExporter() {
        return OtelExporterMocks.spanExporter();
    }

}
