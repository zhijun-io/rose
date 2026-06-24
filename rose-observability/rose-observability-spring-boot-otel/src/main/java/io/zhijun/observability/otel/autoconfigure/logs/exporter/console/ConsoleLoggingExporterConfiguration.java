package io.zhijun.observability.otel.autoconfigure.logs.exporter.console;

import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observability.otel.autoconfigure.logs.exporter.ConditionalOnOpenTelemetryLoggingExporter;

/**
 * Configuration for exporting logs to the console.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ SystemOutLogRecordExporter.class })
@ConditionalOnOpenTelemetryLoggingExporter("console")
public final class ConsoleLoggingExporterConfiguration {

    @Bean
    @ConditionalOnMissingBean
    SystemOutLogRecordExporter consoleLogRecordExporter() {
        return SystemOutLogRecordExporter.create();
    }

}
