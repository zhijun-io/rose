package io.zhijun.observation.boot.autoconfigure.otel.traces.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryPropagationProperties;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryPropagationProperties.PropagationType;

/**
 * Unit test for {@link CompositeTextMapPropagator}.
 */
class CompositeTextMapPropagatorTests {

    @Test
    void collectsAllFields() {
        CompositeTextMapPropagator propagator = new CompositeTextMapPropagator(
                Collections.singletonList(fieldPropagator("a")), Collections.singletonList(fieldPropagator("b")));
        assertThat(propagator.fields()).containsExactly("a", "b");
    }

    @Test
    void injectAllFields() {
        CompositeTextMapPropagator propagator = new CompositeTextMapPropagator(
                Arrays.asList(fieldPropagator("a"), fieldPropagator("b")), Collections.emptyList());
        TextMapSetter<Object> setter = Mockito.mock(TextMapSetter.class);
        Object carrier = new Object();
        propagator.inject(Context.current(), carrier, setter);
        InOrder inOrder = Mockito.inOrder(setter);
        inOrder.verify(setter).set(carrier, "a", "a-value");
        inOrder.verify(setter).set(carrier, "b", "b-value");
    }

    @Test
    void extractUsesFirstMatchingExtractor() {
        CompositeTextMapPropagator propagator = new CompositeTextMapPropagator(
                Collections.emptyList(), Arrays.asList(fieldPropagator("a"), fieldPropagator("b")));
        Map<String, String> carrier = mapOf("a", "a-value", "b", "b-value");
        Context result = propagator.extract(Context.current(), carrier, new MapTextMapGetter());
        assertThat(result).isNotSameAs(Context.current());
    }

    @Test
    void createMapsInjectorsAndExtractors() {
        OpenTelemetryPropagationProperties properties = new OpenTelemetryPropagationProperties();
        properties.setProduce(Collections.singletonList(PropagationType.W3C));
        properties.setConsume(Collections.singletonList(PropagationType.B3));

        TextMapPropagator propagator = CompositeTextMapPropagator.create(properties);
        assertThat(propagator).isInstanceOf(CompositeTextMapPropagator.class);

        CompositeTextMapPropagator composite = (CompositeTextMapPropagator) propagator;
        assertThat(composite.fields()).contains("traceparent", "b3");
    }

    private static TextMapPropagator fieldPropagator(String field) {
        return new TextMapPropagator() {
            @Override
            public java.util.Collection<String> fields() {
                return Collections.singletonList(field);
            }

            @Override
            public <C> void inject(Context context, C carrier, TextMapSetter<C> setter) {
                setter.set(carrier, field, field + "-value");
            }

            @Override
            public <C> Context extract(Context context, C carrier, TextMapGetter<C> getter) {
                String value = getter.get(carrier, field);
                if (value != null) {
                    return context.with(io.opentelemetry.context.ContextKey.named(field), value);
                }
                return context;
            }
        };
    }

    private static Map<String, String> mapOf(String k1, String v1, String k2, String v2) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    private static final class MapTextMapGetter implements TextMapGetter<Map<String, String>> {

        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
            return carrier.keySet();
        }

        @Override
        public String get(Map<String, String> carrier, String key) {
            return carrier == null ? null : carrier.get(key);
        }
    }
}
