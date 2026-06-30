package io.zhijun.spring.beans.factory;

import org.springframework.util.StringUtils;

public class DefaultConfigurationBeanAliasGenerator implements ConfigurationBeanAliasGenerator {

    public String generateAlias(String prefix, String beanName, Class<?> configClass) {
        return configClass.getSimpleName() + StringUtils.capitalize(beanName);
    }
}
