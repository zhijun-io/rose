package io.zhijun.observation.boot.autoconfigure.otel.traces;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryPropagationProperties;
import io.zhijun.observation.boot.autoconfigure.otel.traces.OpenTelemetryPropagationProperties.PropagationType;

class OpenTelemetryPropagationTemplateTests {

    private final OpenTelemetryPropagationTemplate template = new OpenTelemetryPropagationTemplate();

    @Test
    void createsCompositePropagatorFromProperties() {
        OpenTelemetryPropagationProperties properties = new OpenTelemetryPropagationProperties();
        properties.setProduce(Collections.singletonList(PropagationType.W3C));
        properties.setConsume(Collections.singletonList(PropagationType.B3));

        TextMapPropagator propagator = template.create(properties);

        assertThat(propagator).isInstanceOf(CompositeTextMapPropagator.class);
    }

    @Test
    void returnsNoopPropagator() {
        assertThat(template.noop().fields()).isEmpty();
    }
}
