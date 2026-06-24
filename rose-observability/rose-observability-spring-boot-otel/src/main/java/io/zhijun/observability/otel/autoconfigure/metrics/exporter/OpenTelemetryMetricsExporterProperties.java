package io.zhijun.observability.otel.autoconfigure.metrics.exporter;

import java.time.Duration;

import org.springframework.lang.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import io.zhijun.observability.otel.autoconfigure.exporter.ExporterType;
import io.zhijun.observability.otel.autoconfigure.exporter.otlp.OtlpExporterConfig;

/**
 * Configuration properties for OpenTelemetry metrics.
 */
@ConfigurationProperties(prefix = OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX)
public class OpenTelemetryMetricsExporterProperties {

    public static final String CONFIG_PREFIX = "rose.otel.metrics.exporter";

    /**
     * The interval between two consecutive exports of metrics.
     */
    private Duration interval = Duration.ofSeconds(60);

    /**
     * The type of OpenTelemetry exporter to use for metrics.
     */
    @Nullable
    private ExporterType type;

    /**
     * The aggregation temporality to use for exporting metrics.
     */
    private AggregationTemporalityStrategy aggregationTemporality = AggregationTemporalityStrategy.CUMULATIVE;

    /**
     * The aggregation strategy to use for exporting histograms.
     */
    private HistogramAggregationStrategy histogramAggregation = HistogramAggregationStrategy.EXPLICIT_BUCKET_HISTOGRAM;

    /**
     * Options for the OTLP metrics exporter.
     */
    @NestedConfigurationProperty
    private final OtlpExporterConfig otlp = new OtlpExporterConfig();

    public Duration getInterval() {
        return interval;
    }

    public void setInterval(Duration interval) {
        this.interval = interval;
    }

    @Nullable
    public ExporterType getType() {
        return type;
    }

    public void setType(@Nullable ExporterType type) {
        this.type = type;
    }

    public AggregationTemporalityStrategy getAggregationTemporality() {
        return aggregationTemporality;
    }

    public void setAggregationTemporality(AggregationTemporalityStrategy aggregationTemporality) {
        this.aggregationTemporality = aggregationTemporality;
    }

    public HistogramAggregationStrategy getHistogramAggregation() {
        return histogramAggregation;
    }

    public void setHistogramAggregation(HistogramAggregationStrategy histogramAggregation) {
        this.histogramAggregation = histogramAggregation;
    }

    public OtlpExporterConfig getOtlp() {
        return otlp;
    }

}
