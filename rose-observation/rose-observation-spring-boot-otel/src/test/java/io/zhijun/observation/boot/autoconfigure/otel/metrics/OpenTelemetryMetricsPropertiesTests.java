package io.zhijun.observation.boot.autoconfigure.otel.metrics;

import static io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsProperties.ExemplarFilter;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link OpenTelemetryMetricsProperties}.
 */
class OpenTelemetryMetricsPropertiesTests {

    @Test
    void shouldHaveCorrectConfigPrefix() {
        assertThat(OpenTelemetryMetricsProperties.CONFIG_PREFIX).isEqualTo("rose.otel.metrics");
    }

    @Test
    void shouldCreateInstanceWithDefaultValues() {
        OpenTelemetryMetricsProperties properties = new OpenTelemetryMetricsProperties();
        assertThat(properties.getExemplars().isEnabled()).isTrue();
        assertThat(properties.getExemplars().getFilter()).isEqualTo(ExemplarFilter.TRACE_BASED);
        assertThat(properties.getCardinalityLimit()).isEqualTo(2000);
    }

    @Test
    void shouldUpdateValues() {
        OpenTelemetryMetricsProperties properties = new OpenTelemetryMetricsProperties();
        properties.setCardinalityLimit(3000);
        properties.getExemplars().setEnabled(false);
        properties.getExemplars().setFilter(ExemplarFilter.ALWAYS_ON);

        assertThat(properties.getCardinalityLimit()).isEqualTo(3000);
        assertThat(properties.getExemplars().isEnabled()).isFalse();
        assertThat(properties.getExemplars().getFilter()).isEqualTo(ExemplarFilter.ALWAYS_ON);
    }
}
