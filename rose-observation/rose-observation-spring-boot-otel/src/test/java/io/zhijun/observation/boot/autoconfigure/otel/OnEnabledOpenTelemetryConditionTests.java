package io.zhijun.observation.boot.autoconfigure.otel;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link OnEnabledOpenTelemetryCondition}.
 */
class OnEnabledOpenTelemetryConditionTests {

    private final OnEnabledOpenTelemetryCondition condition = new OnEnabledOpenTelemetryCondition();

    @Test
    void matchWhenPropertyTrueAndAnnotationTrue() {
        ConditionOutcome outcome = getMatchOutcome(true, true);
        assertThat(outcome.isMatch()).isTrue();
        assertThat(outcome.getMessage()).contains("rose.otel.enabled is true and annotation requested enabled to be true");
    }

    @Test
    void noMatchWhenPropertyTrueAndAnnotationFalse() {
        ConditionOutcome outcome = getMatchOutcome(true, false);
        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage()).contains("rose.otel.enabled is true and annotation requested enabled to be false");
    }

    @Test
    void noMatchWhenPropertyFalseAndAnnotationTrue() {
        ConditionOutcome outcome = getMatchOutcome(false, true);
        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage()).contains("rose.otel.enabled is false and annotation requested enabled to be true");
    }

    @Test
    void matchWhenPropertyFalseAndAnnotationFalse() {
        ConditionOutcome outcome = getMatchOutcome(false, false);
        assertThat(outcome.isMatch()).isTrue();
        assertThat(outcome.getMessage()).contains("rose.otel.enabled is false and annotation requested enabled to be false");
    }

    @Test
    void matchWhenPropertyNotSetAndAnnotationTrue() {
        ConditionOutcome outcome = getMatchOutcome(null, true);
        assertThat(outcome.isMatch()).isTrue();
        assertThat(outcome.getMessage()).contains("rose.otel.enabled is true and annotation requested enabled to be true");
    }

    @Test
    void noMatchWhenPropertyNotSetAndAnnotationFalse() {
        ConditionOutcome outcome = getMatchOutcome(null, false);
        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage()).contains("rose.otel.enabled is true and annotation requested enabled to be false");
    }

    private ConditionOutcome getMatchOutcome(Boolean propertyValue, boolean annotationValue) {
        ConditionContext context = mock(ConditionContext.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        MockEnvironment environment = new MockEnvironment();

        if (propertyValue != null) {
            environment.setProperty("rose.otel.enabled", propertyValue.toString());
        }

        when(context.getEnvironment()).thenReturn(environment);
        when(metadata.getAnnotationAttributes(ConditionalOnOpenTelemetry.class.getName()))
                .thenReturn(singletonMap("enabled", annotationValue));

        return condition.getMatchOutcome(context, metadata);
    }


    private static Map<String, Object> singletonMap(String k, Object v) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(k, v);
        return map;
    }

    private static Map<String, String> mapOf(String k1, String v1, String k2, String v2) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

}
