package io.zhijun.local.autoconfigure.bootstrap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import io.zhijun.local.bootstrap.BootstrapMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link OnDevModeCondition}.
 */
class OnDevModeConditionTests {

    private final OnDevModeCondition condition = new OnDevModeCondition();

    private final ConditionContext context = mock(ConditionContext.class);

    private final AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    private String originalModeProperty;

    @BeforeEach
    void setUp() {
        BootstrapMode.clear();
        originalModeProperty = System.getProperty(BootstrapMode.PROPERTY_KEY);
        System.clearProperty(BootstrapMode.PROPERTY_KEY);
        org.mockito.Mockito.when(context.getEnvironment()).thenReturn(new MockEnvironment());
    }

    @AfterEach
    void tearDown() {
        if (originalModeProperty == null) {
            System.clearProperty(BootstrapMode.PROPERTY_KEY);
        } else {
            System.setProperty(BootstrapMode.PROPERTY_KEY, originalModeProperty);
        }
        BootstrapMode.clear();
    }

    @Test
    void matchesWhenDevMode() {
        System.setProperty(BootstrapMode.PROPERTY_KEY, "DEV");

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isTrue();
        assertThat(outcome.getMessage()).contains("dev mode");
    }

    @Test
    void doesNotMatchWhenProdMode() {
        System.setProperty(BootstrapMode.PROPERTY_KEY, "PROD");

        ConditionOutcome outcome = condition.getMatchOutcome(context, metadata);

        assertThat(outcome.isMatch()).isFalse();
        assertThat(outcome.getMessage()).contains("not running in dev mode");
    }

}
