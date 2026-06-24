package io.zhijun.observability.otel.autoconfigure.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.zhijun.observability.otel.autoconfigure.exporter.ExporterType;
import io.zhijun.observability.otel.autoconfigure.exporter.otlp.Compression;
import io.zhijun.observability.otel.autoconfigure.exporter.otlp.Protocol;
import io.zhijun.observability.otel.autoconfigure.metrics.OpenTelemetryMetricsProperties;
import io.zhijun.observability.otel.autoconfigure.metrics.exporter.AggregationTemporalityStrategy;
import io.zhijun.observability.otel.autoconfigure.metrics.exporter.HistogramAggregationStrategy;
import io.zhijun.observability.otel.autoconfigure.traces.OpenTelemetryPropagationProperties.PropagationType;
import io.zhijun.observability.otel.autoconfigure.traces.OpenTelemetryTracingProperties.SamplingStrategy;

/**
 * Converts OpenTelemetry Environment Variable Specification values to Rose configuration types.
 */
class OpenTelemetryEnvironmentPropertyConverters {

    private static final Logger logger = LoggerFactory.getLogger(OpenTelemetryEnvironmentPropertyConverters.class);

    static Function<String, ExporterType> exporterType(String externalKey) {
        Assert.hasText(externalKey, "externalKey cannot be null or empty");
        return value -> {
            ExporterType exporterType;
            String normalized = value.trim().toLowerCase();
            if ("console".equals(normalized)) {
                exporterType = ExporterType.CONSOLE;
            } else if ("none".equals(normalized)) {
                exporterType = ExporterType.NONE;
            } else if ("otlp".equals(normalized)) {
                exporterType = ExporterType.OTLP;
            } else {
                exporterType = null;
            }
            if (exporterType == null) {
                logUnsupportedValue(externalKey, value);
            }
            return exporterType;
        };
    }

    static Function<String, Protocol> protocol(String externalKey) {
        Assert.hasText(externalKey, "externalKey cannot be null or empty");
        return value -> {
            Protocol protocol;
            String normalized = value.trim().toLowerCase();
            if ("grpc".equals(normalized)) {
                protocol = Protocol.GRPC;
            } else if ("http/protobuf".equals(normalized)) {
                protocol = Protocol.HTTP_PROTOBUF;
            } else {
                protocol = null;
            }
            if (protocol == null) {
                logUnsupportedValue(externalKey, value);
            }
            return protocol;
        };
    }

    static Function<String, Compression> compression(String externalKey) {
        Assert.hasText(externalKey, "externalKey cannot be null or empty");
        return value -> {
            Compression compression;
            String normalized = value.trim().toLowerCase();
            if ("gzip".equals(normalized)) {
                compression = Compression.GZIP;
            } else if ("none".equals(normalized)) {
                compression = Compression.NONE;
            } else {
                compression = null;
            }
            if (compression == null) {
                logUnsupportedValue(externalKey, value);
            }
            return compression;
        };
    }

    static Function<String, HistogramAggregationStrategy> histogramAggregation(String externalKey) {
        Assert.hasText(externalKey, "externalKey cannot be null or empty");
        return value -> {
            HistogramAggregationStrategy strategy;
            String normalized = value.trim().toUpperCase();
            if ("BASE2_EXPONENTIAL_BUCKET_HISTOGRAM".equals(normalized)) {
                strategy = HistogramAggregationStrategy.BASE2_EXPONENTIAL_BUCKET_HISTOGRAM;
            } else if ("EXPLICIT_BUCKET_HISTOGRAM".equals(normalized)) {
                strategy = HistogramAggregationStrategy.EXPLICIT_BUCKET_HISTOGRAM;
            } else {
                strategy = null;
            }
            if (strategy == null) {
                logUnsupportedValue(externalKey, value);
            }
            return strategy;
        };
    }

    static Function<String, AggregationTemporalityStrategy> aggregationTemporality(String externalKey) {
        Assert.hasText(externalKey, "externalKey cannot be null or empty");
        return value -> {
            AggregationTemporalityStrategy strategy;
            String normalized = value.trim().toUpperCase();
            if ("CUMULATIVE".equals(normalized)) {
                strategy = AggregationTemporalityStrategy.CUMULATIVE;
            } else if ("DELTA".equals(normalized)) {
                strategy = AggregationTemporalityStrategy.DELTA;
            } else if ("LOWMEMORY".equals(normalized)) {
                strategy = AggregationTemporalityStrategy.LOW_MEMORY;
            } else {
                strategy = null;
            }
            if (strategy == null) {
                logUnsupportedValue(externalKey, value);
            }
            return strategy;
        };
    }

    static Function<String, SamplingStrategy> samplingStrategy(String externalKey) {
        Assert.hasText(externalKey, "externalKey cannot be null or empty");
        return value -> {
            SamplingStrategy strategy;
            String normalized = value.toLowerCase().trim();
            if ("always_on".equals(normalized)) {
                strategy = SamplingStrategy.ALWAYS_ON;
            } else if ("always_off".equals(normalized)) {
                strategy = SamplingStrategy.ALWAYS_OFF;
            } else if ("traceidratio".equals(normalized)) {
                strategy = SamplingStrategy.TRACE_ID_RATIO;
            } else if ("parentbased_always_on".equals(normalized)) {
                strategy = SamplingStrategy.PARENT_BASED_ALWAYS_ON;
            } else if ("parentbased_always_off".equals(normalized)) {
                strategy = SamplingStrategy.PARENT_BASED_ALWAYS_OFF;
            } else if ("parentbased_traceidratio".equals(normalized)) {
                strategy = SamplingStrategy.PARENT_BASED_TRACE_ID_RATIO;
            } else {
                strategy = null;
            }
            if (strategy == null) {
                logUnsupportedValue(externalKey, value);
            }
            return strategy;
        };
    }

    static Function<String, List<PropagationType>> propagationType(String externalKey) {
        Assert.hasText(externalKey, "externalKey cannot be null or empty");
        return value -> {
            Set<PropagationType> propagators = new HashSet<PropagationType>();
            String[] items = value.trim().toLowerCase().split("\\s*,\\s*");
            for (String item : items) {
                PropagationType propagator;
                String trimmed = item.trim();
                if ("baggage".equals(trimmed) || "tracecontext".equals(trimmed)) {
                    propagator = PropagationType.W3C;
                } else if ("b3".equals(trimmed)) {
                    propagator = PropagationType.B3;
                } else if ("b3multi".equals(trimmed)) {
                    propagator = PropagationType.B3_MULTI;
                } else {
                    propagator = null;
                }
                if (propagator == null) {
                    logUnsupportedValue(externalKey, value);
                } else {
                    propagators.add(propagator);
                }
            }
            List<PropagationType> result = new ArrayList<PropagationType>(propagators);
            return CollectionUtils.isEmpty(result) ? null : result;
        };
    }

    static Function<String, OpenTelemetryMetricsProperties.ExemplarFilter> exemplarFilter(String externalKey) {
        Assert.hasText(externalKey, "externalKey cannot be null or empty");
        return value -> {
            OpenTelemetryMetricsProperties.ExemplarFilter filter;
            String normalized = value.trim().toLowerCase();
            if ("always_on".equals(normalized)) {
                filter = OpenTelemetryMetricsProperties.ExemplarFilter.ALWAYS_ON;
            } else if ("always_off".equals(normalized)) {
                filter = OpenTelemetryMetricsProperties.ExemplarFilter.ALWAYS_OFF;
            } else if ("trace_based".equals(normalized)) {
                filter = OpenTelemetryMetricsProperties.ExemplarFilter.TRACE_BASED;
            } else {
                filter = null;
            }
            if (filter == null) {
                logUnsupportedValue(externalKey, value);
            }
            return filter;
        };
    }

    private static void logUnsupportedValue(String externalKey, String value) {
        logger.warn("Unsupported value for {}: {}", externalKey, value);
    }
}
