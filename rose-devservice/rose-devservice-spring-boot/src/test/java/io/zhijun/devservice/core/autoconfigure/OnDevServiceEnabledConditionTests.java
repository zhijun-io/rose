package io.zhijun.devservice.core.autoconfigure;

import java.util.HashMap;
import java.util.Map;

import io.zhijun.devservice.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.core.autoconfigure.OnDevServiceEnabledCondition;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link OnDevServiceEnabledCondition}.
 */
class OnDevServiceEnabledConditionTests {

    private final OnDevServiceEnabledCondition condition = new OnDevServiceEnabledCondition();

    private final MockEnvironment environment = new MockEnvironment();

    private final ConditionContext context = mock(ConditionContext.class);

    @Test
    void shouldMatchWhenGloballyEnabledAndSpecificDevServiceEnabled() {
        environment.setProperty("rose.dev.enabled", "true");
        environment.setProperty("rose.dev.test-service.enabled", "true");
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "test-service");
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isTrue();
        assertThat(outcome.getMessage())
                .contains("rose.dev.test-service.enabled is set to true");
    }

    @Test
    void shouldNotMatchWhenGloballyEnabledButSpecificDevServiceDisabled() {
        environment.setProperty("rose.dev.enabled", "true");
        environment.setProperty("rose.dev.test-service.enabled", "false");
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "test-service");
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage())
                .contains("rose.dev.test-service.enabled is set to false");
    }

    @Test
    void shouldNotMatchWhenGloballyDisabledAndSpecificDevServiceEnabled() {
        environment.setProperty("rose.dev.enabled", "false");
        environment.setProperty("rose.dev.test-service.enabled", "true");
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "test-service");
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage())
                .contains("rose.dev.enabled is set to false");
    }

    @Test
    void shouldNotMatchWhenGloballyDisabledAndSpecificDevServiceDisabled() {
        environment.setProperty("rose.dev.enabled", "false");
        environment.setProperty("rose.dev.test-service.enabled", "false");
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "test-service");
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage())
                .contains("rose.dev.enabled is set to false");
    }

    @Test
    void shouldMatchByDefaultWhenPropertiesAreNotSet() {
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "test-service");
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isTrue();
        assertThat(outcome.getMessage())
                .contains("enabled by default (rose.dev.test-service.enabled is not set)");
    }

    @Test
    void shouldNotMatchWhenDevServicesNameIsEmpty() {
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "");
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage())
                .contains("a valid dev services name is not specified");
    }

    @Test
    void shouldNotMatchWhenDevServicesNameIsBlank() {
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "   ");
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage())
                .contains("a valid dev services name is not specified");
    }

    @Test
    void shouldNotMatchWhenDevServicesNameIsNull() {
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", null);
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage())
                .contains("a valid dev services name is not specified");
    }

    @Test
    void shouldNotMatchWhenAnnotationAttributesAreNull() {
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(null);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage())
                .contains("a valid dev services name is not specified");
    }

    @Test
    void shouldMatchWhenOnlyGlobalPropertyIsSetToTrue() {
        environment.setProperty("rose.dev.enabled", "true");
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "test-service");
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isTrue();
        assertThat(outcome.getMessage())
                .contains("enabled by default (rose.dev.test-service.enabled is not set)");
    }

    @Test
    void shouldMatchWhenOnlyGlobalPropertyIsSetToFalse() {
        environment.setProperty("rose.dev.enabled", "false");
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "test-service");
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage())
                .contains("rose.dev.enabled is set to false");
    }

    @Test
    void shouldMatchWhenOnlySpecificPropertyIsSetToTrue() {
        environment.setProperty("rose.dev.test-service.enabled", "true");
        when(context.getEnvironment()).thenReturn(environment);

        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("value", "test-service");
        when(metadata.getAnnotationAttributes(ConditionalOnDevServiceEnabled.class.getName()))
                .thenReturn(attributes);

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isTrue();
        assertThat(outcome.getMessage())
                .contains("rose.dev.test-service.enabled is set to true");
    }

}
