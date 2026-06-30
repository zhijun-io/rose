package io.zhijun.spring.core.binder;

/**
 * SPI for extra bean aliases on {@link io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBinding} beans.
 * Implementations are loaded from {@code META-INF/spring.factories}.
 */
@FunctionalInterface
public interface ConfigurationBeanAliasGenerator {

    String generateAlias(String prefix, String beanName, Class<?> configClass);
}
