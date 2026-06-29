package io.zhijun.observation.boot.autoconfigure.otel.logs.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.resources.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.logs.OpenTelemetryLoggerProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.logs.OpenTelemetryLoggingProperties;

/**
 * Unit test for {@link OpenTelemetryLoggingTemplate}.
 */
class OpenTelemetryLoggingTemplateTests {

    private final OpenTelemetryLoggingTemplate template = new OpenTelemetryLoggingTemplate();

    @Test
    void shouldCreateLogLimitsFromProperties() {
        OpenTelemetryLoggingProperties properties = new OpenTelemetryLoggingProperties();
        properties.getLimits().setMaxAttributeValueLength(100);
        properties.getLimits().setMaxNumberOfAttributes(50);

        LogLimits logLimits = template.logLimits(properties);

        assertThat(logLimits.getMaxAttributeValueLength()).isEqualTo(100);
        assertThat(logLimits.getMaxNumberOfAttributes()).isEqualTo(50);
    }

    @Test
    void shouldBuildLoggerProviderFromInputs() {
        ObjectProvider<LogRecordProcessor> processors = mock(ObjectProvider.class);
        when(processors.orderedStream()).thenReturn(Stream.of(mock(LogRecordProcessor.class)));

        ObjectProvider<OpenTelemetryLoggerProviderBuilderCustomizer> customizers = mock(ObjectProvider.class);
        when(customizers.orderedStream())
                .thenReturn(Stream.of(builder -> builder.setResource(Resource.empty())));

        SdkLoggerProvider provider = template.buildLoggerProvider(
                Clock.getDefault(), LogLimits.builder().build(), Resource.empty(), processors, customizers);

        assertThat(provider).isNotNull();
    }

    @Test
    void shouldBuildBatchLogRecordProcessor() {
        ObjectProvider<LogRecordExporter> exporters = mock(ObjectProvider.class);
        when(exporters.orderedStream()).thenReturn(Stream.of(SystemOutLogRecordExporter.create()));

        OpenTelemetryLoggingProperties properties = new OpenTelemetryLoggingProperties();

        BatchLogRecordProcessor processor =
                template.batchLogRecordProcessor(properties, exporters, mock(ObjectProvider.class));

        assertThat(processor).isNotNull();
    }
}
