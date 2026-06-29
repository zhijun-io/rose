package io.zhijun.observation.boot.autoconfigure.otel.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

    private static final Map<String, PropagationType> PROPAGATION_MAP;

    static {
        Map<String, PropagationType> map = new LinkedHashMap<>();
        map.put("baggage", PropagationType.W3C);
        map.put("tracecontext", PropagationType.W3C);
        map.put("b3", PropagationType.B3);
        map.put("b3multi", PropagationType.B3_MULTI);
        PROPAGATION_MAP = Collections.unmodifiableMap(map);
    }

    static Function<String, ExporterType> exporterType(String externalKey) {
        Map<String, ExporterType> mapping = new LinkedHashMap<>();
        mapping.put("console", ExporterType.CONSOLE);
        mapping.put("none", ExporterType.NONE);
        mapping.put("otlp", ExporterType.OTLP);
        return matcher(externalKey, mapping, false);
    }

    static Function<String, Protocol> protocol(String externalKey) {
        Map<String, Protocol> mapping = new LinkedHashMap<>();
        mapping.put("grpc", Protocol.GRPC);
        mapping.put("http/protobuf", Protocol.HTTP_PROTOBUF);
        return matcher(externalKey, mapping, false);
    }

    static Function<String, Compression> compression(String externalKey) {
        Map<String, Compression> mapping = new LinkedHashMap<>();
        mapping.put("gzip", Compression.GZIP);
        mapping.put("none", Compression.NONE);
        return matcher(externalKey, mapping, false);
    }

    static Function<String, HistogramAggregationStrategy> histogramAggregation(String externalKey) {
        Map<String, HistogramAggregationStrategy> mapping = new LinkedHashMap<>();
        mapping.put("BASE2_EXPONENTIAL_BUCKET_HISTOGRAM", HistogramAggregationStrategy.BASE2_EXPONENTIAL_BUCKET_HISTOGRAM);
        mapping.put("EXPLICIT_BUCKET_HISTOGRAM", HistogramAggregationStrategy.EXPLICIT_BUCKET_HISTOGRAM);
        return matcher(externalKey, mapping, true);
    }

    static Function<String, AggregationTemporalityStrategy> aggregationTemporality(String externalKey) {
        Map<String, AggregationTemporalityStrategy> mapping = new LinkedHashMap<>();
        mapping.put("CUMULATIVE", AggregationTemporalityStrategy.CUMULATIVE);
        mapping.put("DELTA", AggregationTemporalityStrategy.DELTA);
        mapping.put("LOWMEMORY", AggregationTemporalityStrategy.LOW_MEMORY);
        return matcher(externalKey, mapping, true);
    }

    static Function<String, SamplingStrategy> samplingStrategy(String externalKey) {
        Map<String, SamplingStrategy> mapping = new LinkedHashMap<>();
        mapping.put("always_on", SamplingStrategy.ALWAYS_ON);
        mapping.put("always_off", SamplingStrategy.ALWAYS_OFF);
        mapping.put("traceidratio", SamplingStrategy.TRACE_ID_RATIO);
        mapping.put("parentbased_always_on", SamplingStrategy.PARENT_BASED_ALWAYS_ON);
        mapping.put("parentbased_always_off", SamplingStrategy.PARENT_BASED_ALWAYS_OFF);
        mapping.put("parentbased_traceidratio", SamplingStrategy.PARENT_BASED_TRACE_ID_RATIO);
        return matcher(externalKey, mapping, false);
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
        Map<String, OpenTelemetryMetricsProperties.ExemplarFilter> mapping =
                new LinkedHashMap<>();
        mapping.put("always_on", OpenTelemetryMetricsProperties.ExemplarFilter.ALWAYS_ON);
        mapping.put("always_off", OpenTelemetryMetricsProperties.ExemplarFilter.ALWAYS_OFF);
        mapping.put("trace_based", OpenTelemetryMetricsProperties.ExemplarFilter.TRACE_BASED);
        return matcher(externalKey, mapping, false);
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
