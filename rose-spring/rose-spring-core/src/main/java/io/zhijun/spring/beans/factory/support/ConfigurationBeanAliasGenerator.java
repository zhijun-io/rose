package io.zhijun.spring.beans.factory.support;

/**
 * Generates aliases for configuration beans.
 */
public interface ConfigurationBeanAliasGenerator {

    String generateAlias(String prefix, String beanName, Class<?> configClass);
}
