package io.zhijun.dev.services.autoconfigure.bootstrap;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import io.zhijun.dev.services.bootstrap.BootstrapMode;

/**
 * Determines if the application is running in dev mode.
 *
 * @see ConditionalOnDevMode
 */
class OnDevModeCondition extends SpringBootCondition {

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (BootstrapMode.DEV == BootstrapMode.detect()) {
            return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnDevMode.class)
                    .because("application is running in dev mode"));
        }
        return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnDevMode.class)
                .because("application is not running in dev mode"));
    }
}
