package io.zhijun.spring.boot.condition;

import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import io.zhijun.spring.core.env.PropertySourcesUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 检查指定属性前缀是否存在于 Environment 中的条件实现。
 */
public class OnPropertyPrefixCondition extends SpringBootCondition {

    static final Class<ConditionalOnPropertyPrefix> ANNOTATION_TYPE = ConditionalOnPropertyPrefix.class;

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        java.util.Map<String, Object> attrs = metadata.getAnnotationAttributes(ANNOTATION_TYPE.getName());
        if (attrs == null) {
            return ConditionOutcome.noMatch("@" + ANNOTATION_TYPE.getSimpleName() + " not found");
        }

        org.springframework.core.annotation.AnnotationAttributes annotationAttributes =
                org.springframework.core.annotation.AnnotationAttributes.fromMap(attrs);

        ConfigurableEnvironment environment = (ConfigurableEnvironment) context.getEnvironment();

        ResolvablePlaceholderAnnotationAttributes<ConditionalOnPropertyPrefix> resolved =
                ResolvablePlaceholderAnnotationAttributes.of(annotationAttributes, ANNOTATION_TYPE, environment);

        String[] prefixValues = resolved.getStringArray("value");

        boolean matched = !PropertySourcesUtils.findPropertyNames(environment, propertyName -> {
            for (String prefix : prefixValues) {
                String normalized = prefix.endsWith(".") ? prefix : prefix + ".";
                if (propertyName.startsWith(normalized)) {
                    return true;
                }
            }
            return false;
        }).isEmpty();

        return matched
                ? ConditionOutcome.match()
                : ConditionOutcome.noMatch("The prefix values "
                        + java.util.Arrays.toString(prefixValues)
                        + " were not found in Environment!");
    }
}
