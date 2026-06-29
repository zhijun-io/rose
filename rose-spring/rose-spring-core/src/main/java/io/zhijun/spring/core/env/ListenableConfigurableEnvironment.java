package io.zhijun.spring.core.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MissingRequiredPropertiesException;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.support.SpringFactoriesLoader;

import io.zhijun.spring.core.env.listener.EnvironmentListener;

/**
 * ConfigurableEnvironment wrapper that publishes property-source change events through
 * {@link ListenableMutablePropertySources}.
 */
public class ListenableConfigurableEnvironment implements ConfigurableEnvironment {

    private final ConfigurableEnvironment delegate;

    private final List<EnvironmentListener> environmentListeners;

    private final MutablePropertySources propertySources;

    public ListenableConfigurableEnvironment(
            ConfigurableEnvironment delegate,
            ApplicationContext applicationContext,
            List<EnvironmentListener> listeners) {
        this.delegate = delegate;
        this.environmentListeners =
                listeners == null ? Collections.emptyList() : new ArrayList<EnvironmentListener>(listeners);
        this.propertySources = new ListenableMutablePropertySources(
                delegate.getPropertySources(), applicationContext, this.environmentListeners);
    }

    public ListenableConfigurableEnvironment(ConfigurableEnvironment delegate, ApplicationContext applicationContext) {
        this(
                delegate,
                applicationContext,
                SpringFactoriesLoader.loadFactories(
                        EnvironmentListener.class,
                        applicationContext == null ? null : applicationContext.getClassLoader()));
    }

    @Override
    public MutablePropertySources getPropertySources() {
        return propertySources;
    }

    @Override
    public Map<String, Object> getSystemProperties() {
        return delegate.getSystemProperties();
    }

    @Override
    public Map<String, Object> getSystemEnvironment() {
        return delegate.getSystemEnvironment();
    }

    @Override
    public void merge(ConfigurableEnvironment parent) {
        delegate.merge(parent);
    }

    @Override
    public String[] getActiveProfiles() {
        return delegate.getActiveProfiles();
    }

    @Override
    public String[] getDefaultProfiles() {
        return delegate.getDefaultProfiles();
    }

    @Override
    public void setActiveProfiles(String... profiles) {
        delegate.setActiveProfiles(profiles);
    }

    @Override
    public void addActiveProfile(String profile) {
        delegate.addActiveProfile(profile);
    }

    @Override
    public void setDefaultProfiles(String... profiles) {
        delegate.setDefaultProfiles(profiles);
    }

    @Override
    public boolean matchesProfiles(String... profileExpressions) {
        return delegate.matchesProfiles(profileExpressions);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean acceptsProfiles(String... profiles) {
        return delegate.acceptsProfiles(profiles);
    }

    @Override
    public boolean acceptsProfiles(Profiles profiles) {
        return delegate.acceptsProfiles(profiles);
    }

    @Override
    public boolean containsProperty(String key) {
        return delegate.containsProperty(key);
    }

    @Override
    public String getProperty(String key) {
        return delegate.getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return delegate.getProperty(key, defaultValue);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return delegate.getProperty(key, targetType);
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return delegate.getProperty(key, targetType, defaultValue);
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        return delegate.getRequiredProperty(key);
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return delegate.getRequiredProperty(key, targetType);
    }

    @Override
    public String resolvePlaceholders(String text) {
        return delegate.resolvePlaceholders(text);
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return delegate.resolveRequiredPlaceholders(text);
    }

    @Override
    public ConfigurableConversionService getConversionService() {
        return delegate.getConversionService();
    }

    @Override
    public void setConversionService(ConfigurableConversionService conversionService) {
        delegate.setConversionService(conversionService);
    }

    @Override
    public void setPlaceholderPrefix(String placeholderPrefix) {
        delegate.setPlaceholderPrefix(placeholderPrefix);
    }

    @Override
    public void setPlaceholderSuffix(String placeholderSuffix) {
        delegate.setPlaceholderSuffix(placeholderSuffix);
    }

    @Override
    public void setValueSeparator(String valueSeparator) {
        delegate.setValueSeparator(valueSeparator);
    }

    @Override
    public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
        delegate.setIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders);
    }

    @Override
    public void setRequiredProperties(String... requiredProperties) {
        delegate.setRequiredProperties(requiredProperties);
    }

    @Override
    public void validateRequiredProperties() throws MissingRequiredPropertiesException {
        delegate.validateRequiredProperties();
    }

    public ConfigurableEnvironment getDelegate() {
        return delegate;
    }
}
