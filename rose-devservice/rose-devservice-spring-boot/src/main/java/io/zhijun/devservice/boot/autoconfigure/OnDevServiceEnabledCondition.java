package io.zhijun.devservice.boot.autoconfigure;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import io.zhijun.devservice.core.bootstrap.BootstrapMode;

/**
 * Condition for {@link ConditionalOnDevServiceEnabled}.
 */
class OnDevServiceEnabledCondition extends SpringBootCondition {

    private static final String GLOBAL_PROPERTY = DevServiceProperties.ENABLED_PROPERTY;

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(
                ConditionalOnDevServiceEnabled.class.getName());
        String devServicesName = attributes != null ? (String) attributes.get("value") : null;

        if (!StringUtils.hasText(devServicesName)) {
            return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnDevServiceEnabled.class)
                    .because("a valid dev services name is not specified"));
        }

        String globalProperty = context.getEnvironment().getProperty(GLOBAL_PROPERTY);
        String serviceEnabledProperty = DevServiceProperties.serviceEnabledProperty(devServicesName);
        String devServiceProperty = context.getEnvironment().getProperty(serviceEnabledProperty);

        boolean areDevServicesGloballyDisabled = globalProperty != null && !Boolean.parseBoolean(globalProperty);
        boolean isSpecificDevServiceEnabled = Boolean.parseBoolean(devServiceProperty);

        if (areDevServicesGloballyDisabled) {
            return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnDevServiceEnabled.class)
                    .because(GLOBAL_PROPERTY + " is set to false"));
        }

        if (isSpecificDevServiceEnabled) {
            return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnDevServiceEnabled.class)
                    .because(serviceEnabledProperty + " is set to true"));
        }

        if (devServiceProperty == null) {
            if (BootstrapMode.isTest() || BootstrapMode.isDev()) {
                return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnDevServiceEnabled.class)
                        .because("enabled in " + BootstrapMode.detect() + " bootstrap mode ("
                                + serviceEnabledProperty + " is not set)"));
            }
            return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnDevServiceEnabled.class)
                    .because("disabled by default in production (set " + DevServiceProperties.ENABLED_PROPERTY
                            + "=true or " + serviceEnabledProperty + "=true)"));
        }

        return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnDevServiceEnabled.class)
                .because(serviceEnabledProperty + " is set to false"));
    }
}
