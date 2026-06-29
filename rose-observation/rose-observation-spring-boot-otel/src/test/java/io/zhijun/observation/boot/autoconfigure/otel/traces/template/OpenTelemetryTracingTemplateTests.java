package io.zhijun.observation.boot.autoconfigure.otel.traces.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryTracingProperties;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryTracingProperties.SamplingStrategy;

/**
 * Unit test for {@link OpenTelemetryTracingTemplate}.
 */
class OpenTelemetryTracingTemplateTests {

    private final OpenTelemetryTracingTemplate template = new OpenTelemetryTracingTemplate();

    @Test
    void shouldCreateSamplerForConfiguredStrategy() {
        OpenTelemetryTracingProperties properties = new OpenTelemetryTracingProperties();
        properties.getSampling().setStrategy(SamplingStrategy.TRACE_ID_RATIO);
        properties.getSampling().setProbability(0.5f);

        Sampler sampler = template.sampler(properties);

        assertThat(sampler).isEqualTo(Sampler.traceIdRatioBased(0.5));
    }

    @Test
    void shouldCreateCompositeContextPropagators() {
        ObjectProvider<TextMapPropagator> propagators = mock(ObjectProvider.class);
        when(propagators.orderedStream()).thenReturn(Stream.of(firstPropagator(), secondPropagator()));

        assertThat(template.contextPropagators(propagators).getTextMapPropagator().fields())
                .containsExactlyInAnyOrder("x-first", "x-second");
    }

    @Test
    void shouldCreateTracerWithBootScope() {
        OpenTelemetry openTelemetry = mock(OpenTelemetry.class);
        Tracer tracer = mock(Tracer.class);
        when(openTelemetry.getTracer(eq("org.springframework.boot"), anyString())).thenReturn(tracer);

        assertThat(template.tracer(openTelemetry)).isSameAs(tracer);
    }

    private static TextMapPropagator firstPropagator() {
        return new FixedFieldsPropagator("x-first");
    }

    private static TextMapPropagator secondPropagator() {
        return new FixedFieldsPropagator("x-second");
    }

    private static final class FixedFieldsPropagator implements TextMapPropagator {

        private final Set<String> fields;

        private FixedFieldsPropagator(String field) {
            this.fields = Collections.unmodifiableSet(new HashSet<String>(Collections.singletonList(field)));
        }

        @Override
        public Collection<String> fields() {
            return fields;
        }

        @Override
        public <C> void inject(Context context, C carrier, TextMapSetter<C> setter) {}

        @Override
        public <C> Context extract(Context context, C carrier, TextMapGetter<C> getter) {
            return context;
        }
    }
}
