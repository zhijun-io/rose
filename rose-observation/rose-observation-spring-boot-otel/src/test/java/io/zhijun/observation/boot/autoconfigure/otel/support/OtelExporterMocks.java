package io.zhijun.observation.boot.autoconfigure.otel.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Mockito exporters that return successful {@link CompletableResultCode} for async processor shutdown.
 */
public final class OtelExporterMocks {

    private OtelExporterMocks() {}

    public static SpanExporter spanExporter() {
        SpanExporter exporter = mock(SpanExporter.class);
        when(exporter.export(any(Collection.class))).thenReturn(CompletableResultCode.ofSuccess());
        when(exporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
        return exporter;
    }

    public static LogRecordExporter logRecordExporter() {
        LogRecordExporter exporter = mock(LogRecordExporter.class);
        when(exporter.export(any(Collection.class))).thenReturn(CompletableResultCode.ofSuccess());
        when(exporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
        return exporter;
    }
}
