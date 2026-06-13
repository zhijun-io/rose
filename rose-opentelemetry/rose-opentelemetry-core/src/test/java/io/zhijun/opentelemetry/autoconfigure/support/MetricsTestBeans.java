package io.zhijun.opentelemetry.autoconfigure.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.opentelemetry.autoconfigure.metrics.OpenTelemetryMetricsProperties;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.internal.export.CardinalityLimitSelector;

/**
 * Beans required by metrics exporter auto-configuration tests.
 */
@Configuration(proxyBeanMethods = false)
public final class MetricsTestBeans {

    @Bean
    CardinalityLimitSelector cardinalityLimitSelector() {
        return new CardinalityLimitSelector() {
            @Override
            public int getCardinalityLimit(InstrumentType instrumentType) {
                return CardinalityLimitSelector.defaultCardinalityLimitSelector().getCardinalityLimit(instrumentType);
            }
        };
    }

    @Bean
    OpenTelemetryMetricsProperties openTelemetryMetricsProperties() {
        return new OpenTelemetryMetricsProperties();
    }

}
