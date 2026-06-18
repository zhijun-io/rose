package io.zhijun.spring.core.binder.support;

/**
 * SPI for extra bean aliases on {@link io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBinding} beans.
 * Implementations are loaded from {@code META-INF/spring.factories}.
 */
public interface ConfigurationBeanAliasGenerator {

    String generateAlias(String prefix, String beanName, Class<?> configClass);
}
