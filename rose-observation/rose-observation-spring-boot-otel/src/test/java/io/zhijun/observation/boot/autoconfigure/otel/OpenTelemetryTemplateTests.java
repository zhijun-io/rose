package io.zhijun.observation.boot.autoconfigure.otel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class OpenTelemetryTemplateTests {

    private final OpenTelemetryTemplate template = new OpenTelemetryTemplate();

    @Test
    void buildOpenTelemetrySdkAppliesOptionalProviders() {
        SdkLoggerProvider loggerProvider = mock(SdkLoggerProvider.class);
        SdkMeterProvider meterProvider = mock(SdkMeterProvider.class);
        SdkTracerProvider tracerProvider = mock(SdkTracerProvider.class);
        ContextPropagators propagators = mock(ContextPropagators.class);

        OpenTelemetrySdk openTelemetry =
                template.buildOpenTelemetrySdk(
                        objectProvider(loggerProvider),
                        objectProvider(meterProvider),
                        objectProvider(tracerProvider),
                        objectProvider(propagators));

        assertThat(openTelemetry.getSdkLoggerProvider()).isSameAs(loggerProvider);
        assertThat(openTelemetry.getSdkMeterProvider()).isSameAs(meterProvider);
        assertThat(openTelemetry.getSdkTracerProvider()).isSameAs(tracerProvider);
        assertThat(openTelemetry.getPropagators()).isSameAs(propagators);
    }

    @Test
    void noopOpenTelemetryReturnsNoopInstance() {
        assertThat(template.noopOpenTelemetry()).isSameAs(OpenTelemetry.noop());
    }

    @Test
    void clockReturnsDefaultClock() {
        assertThat(template.clock()).isEqualTo(Clock.getDefault());
    }

    @Test
    void resourceReturnsEmptyResource() {
        assertThat(template.resource()).isEqualTo(Resource.empty());
    }

    private static <T> ObjectProvider<T> objectProvider(T value) {
        return new ObjectProvider<T>() {
            @Override
            public T getObject() {
                return value;
            }

            @Override
            public T getObject(Object... args) {
                return value;
            }

            @Override
            public T getIfAvailable() {
                return value;
            }

            @Override
            public T getIfUnique() {
                return value;
            }
        };
    }
}
