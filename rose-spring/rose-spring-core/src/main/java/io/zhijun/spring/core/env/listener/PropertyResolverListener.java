package io.zhijun.spring.core.env.listener;

import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.lang.Nullable;

/**
 * Listener for property resolver operations.
 */
public interface PropertyResolverListener {

    default void beforeGetProperty(
            ConfigurablePropertyResolver propertyResolver,
            String name,
            Class<?> targetType,
            @Nullable Object defaultValue) {}

    default void afterGetProperty(
            ConfigurablePropertyResolver propertyResolver,
            String name,
            Class<?> targetType,
            @Nullable Object value,
            @Nullable Object defaultValue) {}

    default void beforeGetRequiredProperty(
            ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType) {}

    default void afterGetRequiredProperty(
            ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType, Object value) {}

    default void beforeResolvePlaceholders(ConfigurablePropertyResolver propertyResolver, String text) {}

    default void afterResolvePlaceholders(ConfigurablePropertyResolver propertyResolver, String text, String result) {}

    default void beforeResolveRequiredPlaceholders(ConfigurablePropertyResolver propertyResolver, String text) {}

    default void afterResolveRequiredPlaceholders(
            ConfigurablePropertyResolver propertyResolver, String text, String result) {}

    default void beforeSetRequiredProperties(ConfigurablePropertyResolver propertyResolver, String[] properties) {}

    default void afterSetRequiredProperties(ConfigurablePropertyResolver propertyResolver, String[] properties) {}

    default void beforeValidateRequiredProperties(ConfigurablePropertyResolver propertyResolver) {}

    default void afterValidateRequiredProperties(ConfigurablePropertyResolver propertyResolver) {}

    default void beforeGetConversionService(ConfigurablePropertyResolver propertyResolver) {}

    default void afterGetConversionService(
            ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {}

    default void beforeSetConversionService(
            ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {}

    default void afterSetConversionService(
            ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {}

    default void beforeSetPlaceholderPrefix(ConfigurablePropertyResolver propertyResolver, String prefix) {}

    default void afterSetPlaceholderPrefix(ConfigurablePropertyResolver propertyResolver, String prefix) {}

    default void beforeSetPlaceholderSuffix(ConfigurablePropertyResolver propertyResolver, String suffix) {}

    default void afterSetPlaceholderSuffix(ConfigurablePropertyResolver propertyResolver, String suffix) {}

    default void beforeSetIgnoreUnresolvableNestedPlaceholders(
            ConfigurablePropertyResolver propertyResolver, boolean ignoreUnresolvableNestedPlaceholders) {}

    default void afterSetIgnoreUnresolvableNestedPlaceholders(
            ConfigurablePropertyResolver propertyResolver, boolean ignoreUnresolvableNestedPlaceholders) {}

    default void beforeSetValueSeparator(
            ConfigurablePropertyResolver propertyResolver, @Nullable String valueSeparator) {}

    default void afterSetValueSeparator(
            ConfigurablePropertyResolver propertyResolver, @Nullable String valueSeparator) {}
}
