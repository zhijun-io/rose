package io.zhijun.spring.core.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

/**
 * Default logging listener.
 */
public class LoggingEnvironmentListener implements EnvironmentListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingEnvironmentListener.class);

    private void log(String message) {
        logger.debug(message);
    }

    @Override
    public void beforeGetPropertySources(ConfigurableEnvironment environment) {
        log("beforeGetPropertySources()");
    }

    @Override
    public void afterGetPropertySources(ConfigurableEnvironment environment, MutablePropertySources propertySources) {
        log("afterGetPropertySources()");
    }

    @Override
    public void beforeGetSystemProperties(ConfigurableEnvironment environment) {
        log("beforeGetSystemProperties()");
    }

    @Override
    public void afterGetSystemProperties(ConfigurableEnvironment environment, java.util.Map<String, Object> systemProperties) {
        log("afterGetSystemProperties()");
    }

    @Override
    public void beforeGetSystemEnvironment(ConfigurableEnvironment environment) {
        log("beforeGetSystemEnvironment()");
    }

    @Override
    public void afterGetSystemEnvironment(ConfigurableEnvironment environment, java.util.Map<String, Object> systemEnvironmentVariables) {
        log("afterGetSystemEnvironment()");
    }

    @Override
    public void beforeMerge(ConfigurableEnvironment environment, ConfigurableEnvironment parentEnvironment) {
        log("beforeMerge()");
    }

    @Override
    public void afterMerge(ConfigurableEnvironment environment, ConfigurableEnvironment parentEnvironment) {
        log("afterMerge()");
    }

    @Override
    public void beforeGetActiveProfiles(Environment environment) {
        log("beforeGetActiveProfiles()");
    }

    @Override
    public void afterGetActiveProfiles(Environment environment, String[] activeProfiles) {
        log("afterGetActiveProfiles()");
    }

    @Override
    public void beforeGetDefaultProfiles(Environment environment) {
        log("beforeGetDefaultProfiles()");
    }

    @Override
    public void afterGetDefaultProfiles(Environment environment, String[] defaultProfiles) {
        log("afterGetDefaultProfiles()");
    }

    @Override
    public void beforeSetActiveProfiles(ConfigurableEnvironment environment, String[] profiles) {
        log("beforeSetActiveProfiles()");
    }

    @Override
    public void afterSetActiveProfiles(ConfigurableEnvironment environment, String[] profiles) {
        log("afterSetActiveProfiles()");
    }

    @Override
    public void beforeAddActiveProfile(ConfigurableEnvironment environment, String profile) {
        log("beforeAddActiveProfile()");
    }

    @Override
    public void afterAddActiveProfile(ConfigurableEnvironment environment, String profile) {
        log("afterAddActiveProfile()");
    }

    @Override
    public void beforeSetDefaultProfiles(ConfigurableEnvironment environment, String[] profiles) {
        log("beforeSetDefaultProfiles()");
    }

    @Override
    public void afterSetDefaultProfiles(ConfigurableEnvironment environment, String[] profiles) {
        log("afterSetDefaultProfiles()");
    }

    @Override
    public void beforeGetProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType, Object defaultValue) {
        log("beforeGetProperty()");
    }

    @Override
    public void afterGetProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType, Object value, Object defaultValue) {
        log("afterGetProperty()");
    }

    @Override
    public void beforeGetRequiredProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType) {
        log("beforeGetRequiredProperty()");
    }

    @Override
    public void afterGetRequiredProperty(ConfigurablePropertyResolver propertyResolver, String name, Class<?> targetType, Object value) {
        log("afterGetRequiredProperty()");
    }

    @Override
    public void beforeResolvePlaceholders(ConfigurablePropertyResolver propertyResolver, String text) {
        log("beforeResolvePlaceholders()");
    }

    @Override
    public void afterResolvePlaceholders(ConfigurablePropertyResolver propertyResolver, String text, String result) {
        log("afterResolvePlaceholders()");
    }

    @Override
    public void beforeResolveRequiredPlaceholders(ConfigurablePropertyResolver propertyResolver, String text) {
        log("beforeResolveRequiredPlaceholders()");
    }

    @Override
    public void afterResolveRequiredPlaceholders(ConfigurablePropertyResolver propertyResolver, String text, String result) {
        log("afterResolveRequiredPlaceholders()");
    }

    @Override
    public void beforeSetRequiredProperties(ConfigurablePropertyResolver propertyResolver, String[] properties) {
        log("beforeSetRequiredProperties()");
    }

    @Override
    public void afterSetRequiredProperties(ConfigurablePropertyResolver propertyResolver, String[] properties) {
        log("afterSetRequiredProperties()");
    }

    @Override
    public void beforeValidateRequiredProperties(ConfigurablePropertyResolver propertyResolver) {
        log("beforeValidateRequiredProperties()");
    }

    @Override
    public void afterValidateRequiredProperties(ConfigurablePropertyResolver propertyResolver) {
        log("afterValidateRequiredProperties()");
    }

    @Override
    public void beforeGetConversionService(ConfigurablePropertyResolver propertyResolver) {
        log("beforeGetConversionService()");
    }

    @Override
    public void afterGetConversionService(ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {
        log("afterGetConversionService()");
    }

    @Override
    public void beforeSetConversionService(ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {
        log("beforeSetConversionService()");
    }

    @Override
    public void afterSetConversionService(ConfigurablePropertyResolver propertyResolver, ConfigurableConversionService conversionService) {
        log("afterSetConversionService()");
    }

    @Override
    public void beforeSetPlaceholderPrefix(ConfigurablePropertyResolver propertyResolver, String prefix) {
        log("beforeSetPlaceholderPrefix()");
    }

    @Override
    public void afterSetPlaceholderPrefix(ConfigurablePropertyResolver propertyResolver, String prefix) {
        log("afterSetPlaceholderPrefix()");
    }

    @Override
    public void beforeSetPlaceholderSuffix(ConfigurablePropertyResolver propertyResolver, String suffix) {
        log("beforeSetPlaceholderSuffix()");
    }

    @Override
    public void afterSetPlaceholderSuffix(ConfigurablePropertyResolver propertyResolver, String suffix) {
        log("afterSetPlaceholderSuffix()");
    }

    @Override
    public void beforeSetIgnoreUnresolvableNestedPlaceholders(ConfigurablePropertyResolver propertyResolver, boolean ignoreUnresolvableNestedPlaceholders) {
        log("beforeSetIgnoreUnresolvableNestedPlaceholders()");
    }

    @Override
    public void afterSetIgnoreUnresolvableNestedPlaceholders(ConfigurablePropertyResolver propertyResolver, boolean ignoreUnresolvableNestedPlaceholders) {
        log("afterSetIgnoreUnresolvableNestedPlaceholders()");
    }

    @Override
    public void beforeSetValueSeparator(ConfigurablePropertyResolver propertyResolver, String valueSeparator) {
        log("beforeSetValueSeparator()");
    }

    @Override
    public void afterSetValueSeparator(ConfigurablePropertyResolver propertyResolver, String valueSeparator) {
        log("afterSetValueSeparator()");
    }
}
