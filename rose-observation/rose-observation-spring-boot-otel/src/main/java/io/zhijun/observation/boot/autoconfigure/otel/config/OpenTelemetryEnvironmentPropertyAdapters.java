package io.zhijun.observation.boot.autoconfigure.otel.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.zhijun.observation.boot.autoconfigure.otel.OpenTelemetryProperties;
import io.zhijun.observation.boot.autoconfigure.otel.common.*;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsProperties;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsExporterProperties;
import io.zhijun.observation.boot.autoconfigure.otel.resource.OpenTelemetryResourceProperties;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryPropagationProperties;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryTracingProperties;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryTracingExporterProperties;

/**
 * Adapters from the OpenTelemetry Environment Variable Specification to {@code rose.otel.*} properties.
 *
 * @see <a href="https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/">OTel env spec</a>
 */
class OpenTelemetryEnvironmentPropertyAdapters {

    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)(ms|s|m|h)$");

    static Map<String, Object> general(ConfigurableEnvironment environment) {
        return map(environment)
                .mapProperty(
                        "otel.sdk.disabled",
                        OpenTelemetryProperties.ENABLED_PROPERTY,
                        value -> !Boolean.parseBoolean(value.toLowerCase()))
                .mapMap("otel.resource.attributes", OpenTelemetryResourceProperties.ATTRIBUTES_PROPERTY)
                .mapString("otel.service.name", OpenTelemetryResourceProperties.SERVICE_NAME_PROPERTY)
                .mapEnum(
                        "otel.propagators",
                        OpenTelemetryPropagationProperties.CONFIG_PREFIX + ".produce",
                        OpenTelemetryEnvironmentPropertyConverters::propagationType)
                .mapEnum(
                        "otel.tracer.sampler",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".sampling.strategy",
                        OpenTelemetryEnvironmentPropertyConverters::samplingStrategy)
                .mapDouble(
                        "otel.tracer.sampler.arg",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".sampling.probability")
                .build();
    }

    static Map<String, Object> batchSpanProcessor(ConfigurableEnvironment environment) {
        return map(environment)
                .mapDuration(
                        "otel.bsp.schedule.delay",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.schedule-delay")
                .mapDuration(
                        "otel.bsp.export.timeout",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.export-timeout")
                .mapInteger(
                        "otel.bsp.max.queue.size",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.max-queue-size")
                .mapInteger(
                        "otel.bsp.max.export.batch.size",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".processor.max-export-batch-size")
                .build();
    }

    static Map<String, Object> attributeLimits(ConfigurableEnvironment environment) {
        return map(environment)
                .mapInteger(
                        "otel.attribute.value.length.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-attribute-value-length")
                .mapInteger(
                        "otel.attribute.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-attributes")
                .build();
    }

    static Map<String, Object> spanLimits(ConfigurableEnvironment environment) {
        return map(environment)
                .mapInteger(
                        "otel.span.attribute.value.length.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-attribute-value-length")
                .mapInteger(
                        "otel.span.attribute.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-attributes")
                .mapInteger(
                        "otel.span.event.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-events")
                .mapInteger(
                        "otel.span.link.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-links")
                .mapInteger(
                        "otel.event.attribute.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-attributes-per-event")
                .mapInteger(
                        "otel.link.attribute.count.limit",
                        OpenTelemetryTracingProperties.CONFIG_PREFIX + ".limits.max-number-of-attributes-per-link")
                .build();
    }

    static Map<String, Object> exporterSelection(ConfigurableEnvironment environment) {
        return map(environment)
                .mapEnum(
                        "otel.traces.exporter",
                        OpenTelemetryTracingExporterProperties.TYPE_PROPERTY,
                        OpenTelemetryEnvironmentPropertyConverters::exporterType)
                .mapEnum(
                        "otel.metrics.exporter",
                        OpenTelemetryMetricsExporterProperties.TYPE_PROPERTY,
                        OpenTelemetryEnvironmentPropertyConverters::exporterType)
                .build();
    }

    static Map<String, Object> metrics(ConfigurableEnvironment environment) {
        return map(environment)
                .mapEnum(
                        "otel.metrics.exemplar.filter",
                        OpenTelemetryMetricsProperties.CONFIG_PREFIX + ".exemplars.filter",
                        OpenTelemetryEnvironmentPropertyConverters::exemplarFilter)
                .mapDuration("otel.metric.export.interval", OpenTelemetryMetricsExporterProperties.INTERVAL_PROPERTY)
                .mapDuration(
                        "otel.metric.export.timeout",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.timeout")
                .build();
    }

    static Map<String, Object> otlpExporter(ConfigurableEnvironment environment) {
        return map(environment)
                .mapEnum(
                        "otel.exporter.otlp.protocol",
                        OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.protocol",
                        OpenTelemetryEnvironmentPropertyConverters::protocol)
                .mapString(
                        "otel.exporter.otlp.endpoint", OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.endpoint")
                .mapMap("otel.exporter.otlp.headers", OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.headers")
                .mapEnum(
                        "otel.exporter.otlp.compression",
                        OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.compression",
                        OpenTelemetryEnvironmentPropertyConverters::compression)
                .mapDuration(
                        "otel.exporter.otlp.timeout", OpenTelemetryExporterProperties.CONFIG_PREFIX + ".otlp.timeout")
                .mapEnum(
                        "otel.exporter.otlp.metrics.protocol",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.protocol",
                        OpenTelemetryEnvironmentPropertyConverters::protocol)
                .mapString(
                        "otel.exporter.otlp.metrics.endpoint",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.endpoint")
                .mapMap(
                        "otel.exporter.otlp.metrics.headers",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.headers")
                .mapEnum(
                        "otel.exporter.otlp.metrics.compression",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.compression",
                        OpenTelemetryEnvironmentPropertyConverters::compression)
                .mapDuration(
                        "otel.exporter.otlp.metrics.timeout",
                        OpenTelemetryMetricsExporterProperties.CONFIG_PREFIX + ".otlp.timeout")
                .mapEnum(
                        "otel.exporter.otlp.traces.protocol",
                        OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".otlp.protocol",
                        OpenTelemetryEnvironmentPropertyConverters::protocol)
                .mapString(
                        "otel.exporter.otlp.traces.endpoint",
                        OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".otlp.endpoint")
                .mapMap(
                        "otel.exporter.otlp.traces.headers",
                        OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".otlp.headers")
                .mapEnum(
                        "otel.exporter.otlp.traces.compression",
                        OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".otlp.compression",
                        OpenTelemetryEnvironmentPropertyConverters::compression)
                .mapDuration(
                        "otel.exporter.otlp.traces.timeout",
                        OpenTelemetryTracingExporterProperties.CONFIG_PREFIX + ".otlp.timeout")
                .build();
    }

    private static Mapper map(ConfigurableEnvironment environment) {
        Assert.notNull(environment, "environment cannot be null");
        return new Mapper(environment);
    }

    private static final class Mapper {

        private final ConfigurableEnvironment environment;

        private final Map<String, Object> properties = new HashMap<String, Object>();

        private Mapper(ConfigurableEnvironment environment) {
            this.environment = environment;
        }

        private <T> Mapper mapProperty(String externalKey, String roseKey, Function<String, T> converter) {
            Assert.hasText(externalKey, "externalKey cannot be null or empty");
            Assert.hasText(roseKey, "roseKey cannot be null or empty");
            Assert.notNull(converter, "converter cannot be null");

            String value = environment.getProperty(externalKey);
            if (StringUtils.hasText(value)) {
                T convertedValue = converter.apply(value.trim());
                if (convertedValue != null) {
                    properties.put(roseKey, convertedValue);
                }
            }
            return this;
        }

        private <T> Mapper mapEnum(
                String externalKey, String roseKey, Function<String, Function<String, T>> converterFactory) {
            Assert.notNull(converterFactory, "converterFactory cannot be null");
            return mapProperty(externalKey, roseKey, converterFactory.apply(externalKey));
        }

        private Mapper mapString(String externalKey, String roseKey) {
            return mapProperty(externalKey, roseKey, value -> value);
        }

        private Mapper mapDouble(String externalKey, String roseKey) {
            return mapProperty(externalKey, roseKey, value -> {
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException ex) {
                    return null;
                }
            });
        }

        private Mapper mapInteger(String externalKey, String roseKey) {
            return mapProperty(externalKey, roseKey, value -> {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException ex) {
                    return null;
                }
            });
        }

        private Mapper mapDuration(String externalKey, String roseKey) {
            return mapProperty(externalKey, roseKey, value -> {
                try {
                    Matcher matcher = DURATION_PATTERN.matcher(value);
                    if (matcher.matches()) {
                        long amount = Long.parseLong(matcher.group(1));
                        String unit = matcher.group(2);
                        if ("ms".equals(unit)) {
                            return Duration.ofMillis(amount);
                        }
                        if ("s".equals(unit)) {
                            return Duration.ofSeconds(amount);
                        }
                        if ("m".equals(unit)) {
                            return Duration.ofMinutes(amount);
                        }
                        if ("h".equals(unit)) {
                            return Duration.ofHours(amount);
                        }
                    }
                    return Duration.ofMillis(Long.parseLong(value));
                } catch (Exception ex) {
                    return null;
                }
            });
        }

        private Mapper mapMap(String externalKey, String roseKey) {
            return mapMap(externalKey, roseKey, null);
        }

        private Mapper mapMap(
                String externalKey, String roseKey, Function<Map<String, String>, Map<String, String>> postProcessor) {
            return mapProperty(externalKey, roseKey, value -> {
                Map<String, String> propertyMap = new HashMap<String, String>();
                String[] keyValuePairs = StringUtils.tokenizeToStringArray(value, ",");
                for (String pair : keyValuePairs) {
                    String[] entry = pair.split("=", 2);
                    if (entry.length == 2 && StringUtils.hasText(entry[0]) && StringUtils.hasText(entry[1])) {
                        propertyMap.put(
                                entry[0].trim(), StringUtils.uriDecode(entry[1].trim(), StandardCharsets.UTF_8));
                    }
                }
                Map<String, String> result = propertyMap;
                if (postProcessor != null) {
                    result = postProcessor.apply(propertyMap);
                }
                return CollectionUtils.isEmpty(result) ? null : result;
            });
        }

        private Map<String, Object> build() {
            return properties;
        }
    }
}
