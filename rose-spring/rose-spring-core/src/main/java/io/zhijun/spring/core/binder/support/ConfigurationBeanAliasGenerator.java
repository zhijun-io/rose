package io.zhijun.spring.core.binder.support;

/**
 * Generates aliases for configuration beans.
 */
public interface ConfigurationBeanAliasGenerator {

    String generateAlias(String prefix, String beanName, Class<?> configClass);
}
