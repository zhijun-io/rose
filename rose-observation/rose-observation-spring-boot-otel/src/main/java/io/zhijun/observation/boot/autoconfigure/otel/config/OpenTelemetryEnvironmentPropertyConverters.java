package io.zhijun.observation.boot.autoconfigure.otel.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.ExporterType;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Compression;
import io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp.Protocol;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.AggregationTemporalityStrategy;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.exporter.HistogramAggregationStrategy;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryPropagationProperties.PropagationType;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryTracingProperties.SamplingStrategy;

/**
 * Converts OpenTelemetry Environment Variable Specification values to Rose configuration types.
 */
class OpenTelemetryEnvironmentPropertyConverters {

    private static final Logger logger = LoggerFactory.getLogger(OpenTelemetryEnvironmentPropertyConverters.class);

    private static final Map<String, PropagationType> PROPAGATION_MAP = Map.of(
            "baggage", PropagationType.W3C,
            "tracecontext", PropagationType.W3C,
            "b3", PropagationType.B3,
            "b3multi", PropagationType.B3_MULTI);

    static Function<String, ExporterType> exporterType(String externalKey) {
        return matcher(
                externalKey,
                Map.of(
                        "console", ExporterType.CONSOLE,
                        "none", ExporterType.NONE,
                        "otlp", ExporterType.OTLP),
                false);
    }

    static Function<String, Protocol> protocol(String externalKey) {
        return matcher(
                externalKey,
                Map.of("grpc", Protocol.GRPC, "http/protobuf", Protocol.HTTP_PROTOBUF),
                false);
    }

    static Function<String, Compression> compression(String externalKey) {
        return matcher(
                externalKey, Map.of("gzip", Compression.GZIP, "none", Compression.NONE), false);
    }

    static Function<String, HistogramAggregationStrategy> histogramAggregation(String externalKey) {
        return matcher(
                externalKey,
                Map.of(
                        "BASE2_EXPONENTIAL_BUCKET_HISTOGRAM", HistogramAggregationStrategy.BASE2_EXPONENTIAL_BUCKET_HISTOGRAM,
                        "EXPLICIT_BUCKET_HISTOGRAM", HistogramAggregationStrategy.EXPLICIT_BUCKET_HISTOGRAM),
                true);
    }

    static Function<String, AggregationTemporalityStrategy> aggregationTemporality(String externalKey) {
        return matcher(
                externalKey,
                Map.of(
                        "CUMULATIVE", AggregationTemporalityStrategy.CUMULATIVE,
                        "DELTA", AggregationTemporalityStrategy.DELTA,
                        "LOWMEMORY", AggregationTemporalityStrategy.LOW_MEMORY),
                true);
    }

    static Function<String, SamplingStrategy> samplingStrategy(String externalKey) {
        return matcher(
                externalKey,
                Map.of(
                        "always_on", SamplingStrategy.ALWAYS_ON,
                        "always_off", SamplingStrategy.ALWAYS_OFF,
                        "traceidratio", SamplingStrategy.TRACE_ID_RATIO,
                        "parentbased_always_on", SamplingStrategy.PARENT_BASED_ALWAYS_ON,
                        "parentbased_always_off", SamplingStrategy.PARENT_BASED_ALWAYS_OFF,
                        "parentbased_traceidratio", SamplingStrategy.PARENT_BASED_TRACE_ID_RATIO),
                false);
    }

    static Function<String, List<PropagationType>> propagationType(String externalKey) {
        Assert.hasText(externalKey, "externalKey cannot be null or empty");
        return value -> {
            Set<PropagationType> propagators = new HashSet<PropagationType>();
            for (String item : value.trim().toLowerCase().split("\\s*,\\s*")) {
                PropagationType propagator = PROPAGATION_MAP.get(item.trim());
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
        return matcher(
                externalKey,
                Map.of(
                        "always_on", OpenTelemetryMetricsProperties.ExemplarFilter.ALWAYS_ON,
                        "always_off", OpenTelemetryMetricsProperties.ExemplarFilter.ALWAYS_OFF,
                        "trace_based", OpenTelemetryMetricsProperties.ExemplarFilter.TRACE_BASED),
                false);
    }

    private static <E> Function<String, E> matcher(String externalKey, Map<String, E> mapping, boolean upper) {
        Assert.hasText(externalKey, "externalKey cannot be null or empty");
        return value -> {
            String normalized = upper ? value.trim().toUpperCase() : value.trim().toLowerCase();
            E result = mapping.get(normalized);
            if (result == null) {
                logUnsupportedValue(externalKey, value);
            }
            return result;
        };
    }

    private static void logUnsupportedValue(String externalKey, String value) {
        logger.warn("Unsupported value for {}: {}", externalKey, value);
    }
}
