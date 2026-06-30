package io.zhijun.spring.core.binder;

import org.springframework.util.StringUtils;

/**
 * Default alias generator: {@code SimpleClassName + Capitalized(beanName)}.
 */
public final class DefaultConfigurationBeanAliasGenerator {

    public String generateAlias(String prefix, String beanName, Class<?> configClass) {
        return configClass.getSimpleName() + StringUtils.capitalize(beanName);
    }
}
