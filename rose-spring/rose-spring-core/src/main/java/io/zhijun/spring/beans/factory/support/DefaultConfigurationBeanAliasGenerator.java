package io.zhijun.spring.beans.factory.support;

import org.springframework.util.StringUtils;

/**
 * Default alias generator: {@code SimpleClassName + Capitalized(beanName)}.
 */
public class DefaultConfigurationBeanAliasGenerator implements ConfigurationBeanAliasGenerator {

    @Override
    public String generateAlias(String prefix, String beanName, Class<?> configClass) {
        return configClass.getSimpleName() + StringUtils.capitalize(beanName);
    }
}
