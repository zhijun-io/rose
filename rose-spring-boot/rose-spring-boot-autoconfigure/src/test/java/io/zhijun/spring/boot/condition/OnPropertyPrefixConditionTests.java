package io.zhijun.spring.boot.condition;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OnPropertyPrefixConditionTests {

    private final OnPropertyPrefixCondition condition = new OnPropertyPrefixCondition();

    @Test
    void shouldMatchWhenPrefixFound() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.datasource.url", "jdbc:test");
        ConditionOutcome outcome = condition.getMatchOutcome(createContext(environment),
                createMetadata("spring.datasource"));
        assertThat(outcome.isMatch()).isTrue();
    }

    @Test
    void shouldMatchWhenPrefixFoundWithTrailingDot() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.datasource.url", "jdbc:test");
        ConditionOutcome outcome = condition.getMatchOutcome(createContext(environment),
                createMetadata("spring.datasource."));
        assertThat(outcome.isMatch()).isTrue();
    }

    @Test
    void shouldNotMatchWhenPrefixNotFound() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("other.prefix.key", "value");
        ConditionOutcome outcome = condition.getMatchOutcome(createContext(environment),
                createMetadata("spring.datasource"));
        assertThat(outcome.isMatch()).isFalse();
    }

    @Test
    void shouldNotMatchWhenAnnotationNotFound() {
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(metadata.getAnnotationAttributes(ConditionalOnPropertyPrefix.class.getName())).thenReturn(null);
        ConditionOutcome outcome = condition.getMatchOutcome(createContext(new MockEnvironment()), metadata);
        assertThat(outcome.isMatch()).isFalse();
    }

    private static ConditionContext createContext(ConfigurableEnvironment environment) {
        ConditionContext context = mock(ConditionContext.class);
        when(context.getEnvironment()).thenReturn(environment);
        return context;
    }

    private static AnnotatedTypeMetadata createMetadata(String... prefixValues) {
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        java.util.Map<String, Object> attrs = new java.util.LinkedHashMap<>();
        attrs.put("value", prefixValues);
        when(metadata.getAnnotationAttributes(ConditionalOnPropertyPrefix.class.getName())).thenReturn(attrs);
        return metadata;
    }
}
