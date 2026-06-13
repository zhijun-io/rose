package io.zhijun.opentelemetry.autoconfigure.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.resources.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.opentelemetry.autoconfigure.support.OpenTelemetryTestBeans;
import io.zhijun.opentelemetry.autoconfigure.support.TracingTestBeans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link OpenTelemetryMetricsAutoConfiguration}.
 */
class OpenTelemetryMetricsAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenTelemetryMetricsAutoConfiguration.class))
            .withUserConfiguration(OpenTelemetryTestBeans.class);

    @Test
    void autoConfigurationNotActivatedWhenOpenTelemetryDisabled() {
        contextRunner
            .withPropertyValues("rose.otel.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(SdkMeterProvider.class));
    }

    @Test
    void autoConfigurationNotActivatedWhenMetricsDisabled() {
        contextRunner
            .withPropertyValues("rose.otel.metrics.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(SdkMeterProvider.class));
    }

    @Test
    void meterProviderAvailableWithDefaultConfiguration() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SdkMeterProvider.class);
            assertThat(context).hasSingleBean(CardinalityLimitSelector.class);
            assertThat(context).hasSingleBean(Meter.class);
        });
    }

    @Test
    void cardinalityLimitSelectorConfigurationApplied() {
        contextRunner
                .withPropertyValues("rose.otel.metrics.cardinality-limit=200")
                .run(context -> {
                    CardinalityLimitSelector cardinalityLimitSelector = context.getBean(CardinalityLimitSelector.class);
                    assertThat(cardinalityLimitSelector.getCardinalityLimit(InstrumentType.COUNTER)).isEqualTo(200);
                });
    }

    @Test
    void customCardinalityLimitSelectorAvailable() {
        contextRunner
            .withUserConfiguration(CustomCardinalityLimitSelectorConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(CardinalityLimitSelector.class);
                assertThat(context.getBean(CardinalityLimitSelector.class))
                    .isSameAs(context.getBean(CustomCardinalityLimitSelectorConfiguration.class).customCardinalityLimitSelector());
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomCardinalityLimitSelectorConfiguration {

        private final CardinalityLimitSelector customCardinalityLimitSelector = mock(CardinalityLimitSelector.class);

        @Bean
        CardinalityLimitSelector customCardinalityLimitSelector() {
            return customCardinalityLimitSelector;
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class CustomMetricBuilderCustomizerConfiguration {

        private final OpenTelemetryMeterProviderBuilderCustomizer customMetricBuilderCustomizer =
                mock(OpenTelemetryMeterProviderBuilderCustomizer.class);

        @Bean
        OpenTelemetryMeterProviderBuilderCustomizer customMetricBuilderCustomizer() {
            return customMetricBuilderCustomizer;
        }

    }

}
