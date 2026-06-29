package io.zhijun.observation.boot.autoconfigure.otel.support;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsProperties;

/**
 * Beans required by metrics exporter auto-configuration test.
 */
@Configuration(proxyBeanMethods = false)
public final class MetricsTestBeans {

    @Bean
    CardinalityLimitSelector cardinalityLimitSelector() {
        return new CardinalityLimitSelector() {
            @Override
            public int getCardinalityLimit(InstrumentType instrumentType) {
                return CardinalityLimitSelector.defaultCardinalityLimitSelector()
                        .getCardinalityLimit(instrumentType);
            }
        };
    }

    @Bean
    OpenTelemetryMetricsProperties openTelemetryMetricsProperties() {
        return new OpenTelemetryMetricsProperties();
    }
}
