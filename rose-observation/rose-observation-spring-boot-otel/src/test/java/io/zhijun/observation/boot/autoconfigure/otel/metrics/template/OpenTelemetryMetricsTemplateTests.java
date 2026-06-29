package io.zhijun.observation.boot.autoconfigure.otel.metrics.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMeterProviderBuilderCustomizer;
import io.zhijun.observation.boot.autoconfigure.otel.metrics.OpenTelemetryMetricsProperties;

/**
 * Unit test for {@link OpenTelemetryMetricsTemplate}.
 */
class OpenTelemetryMetricsTemplateTests {

    private final OpenTelemetryMetricsTemplate template = new OpenTelemetryMetricsTemplate();

    @Test
    void shouldCreateCardinalityLimitSelectorFromProperties() {
        OpenTelemetryMetricsProperties properties = new OpenTelemetryMetricsProperties();
        properties.setCardinalityLimit(1234);

        assertThat(template.cardinalityLimitSelector(properties).getCardinalityLimit(InstrumentType.COUNTER))
                .isEqualTo(1234);
    }

    @Test
    void shouldBuildMeterProviderFromInputs() {
        ObjectProvider<OpenTelemetryMeterProviderBuilderCustomizer> customizers = mock(ObjectProvider.class);
        when(customizers.orderedStream())
                .thenReturn(Stream.of(builder -> builder.setResource(Resource.empty())));

        SdkMeterProvider provider =
                template.buildMeterProvider(Clock.getDefault(), Resource.empty(), customizers);

        assertThat(provider).isNotNull();
    }

    @Test
    void shouldCreateMeterWithBootScope() {
        OpenTelemetry openTelemetry = mock(OpenTelemetry.class);
        Meter meter = mock(Meter.class);
        when(openTelemetry.getMeter(OpenTelemetryMetricsTemplate.INSTRUMENTATION_SCOPE_NAME)).thenReturn(meter);

        assertThat(template.meter(openTelemetry)).isSameAs(meter);
    }
}
