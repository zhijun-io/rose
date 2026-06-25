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

    private static final String GLOBAL_PROPERTY = DevServiceProperties.CONFIG_PREFIX + ".enabled";
    private static final String DEV_SERVICES_PROPERTY = "rose.dev.%s.enabled";

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
        String devServiceProperty = context.getEnvironment().getProperty(
                String.format(DEV_SERVICES_PROPERTY, devServicesName));

        boolean areDevServicesGloballyDisabled = globalProperty != null && !Boolean.parseBoolean(globalProperty);
        boolean isSpecificDevServiceEnabled = Boolean.parseBoolean(devServiceProperty);

        if (areDevServicesGloballyDisabled) {
            return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnDevServiceEnabled.class)
                    .because(GLOBAL_PROPERTY + " is set to false"));
        }

        if (isSpecificDevServiceEnabled) {
            return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnDevServiceEnabled.class)
                    .because(String.format(DEV_SERVICES_PROPERTY, devServicesName) + " is set to true"));
        }

        if (devServiceProperty == null) {
            if (BootstrapMode.isTest() || BootstrapMode.isDev()) {
                return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnDevServiceEnabled.class)
                        .because("enabled in " + BootstrapMode.detect() + " bootstrap mode ("
                                + String.format(DEV_SERVICES_PROPERTY, devServicesName) + " is not set)"));
            }
            return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnDevServiceEnabled.class)
                    .because("disabled by default in production (set rose.dev.enabled=true or rose.dev."
                            + devServicesName + ".enabled=true)"));
        }

        return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnDevServiceEnabled.class)
                .because(String.format(DEV_SERVICES_PROPERTY, devServicesName) + " is set to false"));
    }
}
